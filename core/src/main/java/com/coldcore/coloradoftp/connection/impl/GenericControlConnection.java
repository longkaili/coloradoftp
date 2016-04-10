package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.command.Command;
import com.coldcore.coloradoftp.command.CommandFactory;
import com.coldcore.coloradoftp.command.CommandProcessor;
import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.connection.*;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.Channel;

/**
 * @see com.coldcore.coloradoftp.connection.Connection
 * <p/>
 * Control connections usualy do not eat much network traffic(网络流量) (that is a job of
 * data connections), so there is no need to control network overhead for this type
 * of connection. Control connection always have lower priority than data connections
 * and their execution does not take place on every round of lyfe cycle thread.
 */

/**
 　　控制连接的基本逻辑是从监听端口获得SocketChannel后，就开启一个读线程从socket那里读取用户命令，然后执行;
 开启一个写线程，将命令的执行结果写入Socket传给用户;下面主要关注如何从用户的输入流中解析出命令，及如何把执行完的结果
 返回给用户
 控制连接的优先级比数据连接低*/

public class GenericControlConnection extends GenericConnection implements ControlConnection {

    private static Logger log = Logger.getLogger(GenericControlConnection.class);
    protected ByteArrayOutputStream   warray;//ByteArrayOutputStream:将写入的数据转成字节数组,将数组输出到ByteArray中去
    protected ByteArrayOutputStream   rarray;//
    protected boolean                 rarrayComplete;//数据读取是否完成
    protected StringBuffer            incomingBuffer;//读入字符存放缓冲区
    protected StringBuffer            outgoingBuffer;//写入字符存放缓冲区
    protected boolean                 interruptState;//中断状态
    protected CommandProcessor        commandProcessor;//命令执行对象
    protected CommandFactory          commandFactory;//命令工厂
    protected Session                 session;//用户信息对象(包括用户对应的数据连接，控制连接，SocketChannel对象等信息)
    protected DataConnection          dataConnection;//持有的数据连接
    protected DataConnectionInitiator dataConnectionInitiator;//持有的数据连接初始化对象，可以调用这个对象建立一个与该连接对应的控制连接
    protected boolean                 utf8;//是否UTF-8编码

    public static final String CHARSET_UTF8  = "UTF-8";
    public static final String CHARSET_ASCII = "US-ASCII";

    public static final char UTF8_MAGIC_NUMBER = (char) 65279;//魔数，一个有特别含义的常量


    //构造器
    public GenericControlConnection(int bufferSize) {
        super();

        utf8 = true;//默认是utf8编码

        incomingBuffer = new StringBuffer();//输入缓冲区(字符)
        outgoingBuffer = new StringBuffer();//输出缓冲区(字符)

        rbuffer = ByteBuffer.allocate(bufferSize); //从SocketChannel中读取的字节数组
        rbuffer.flip();

        wbuffer = ByteBuffer.allocate(bufferSize);//输出缓冲区(字节)
        wbuffer.flip();

        warray = new ByteArrayOutputStream();
        rarray = new ByteArrayOutputStream();
    }


    public synchronized void initialize(SocketChannel channel) {
        super.initialize(channel);

        //ObjectFactory cannot be used in a constructor, so we create the rest of the objects here
        //工厂方法获得所有需要的对象
        commandProcessor = (CommandProcessor) ObjectFactory.getObject(ObjectName.COMMAND_PROCESSOR);
        commandFactory = (CommandFactory) ObjectFactory.getObject(ObjectName.COMMAND_FACTORY);
        session = (Session) ObjectFactory.getObject(ObjectName.SESSION);
        dataConnectionInitiator = (DataConnectionInitiator) ObjectFactory.getObject(ObjectName.DATA_CONNECTION_INITIATOR);
        dataConnectionInitiator.setControlConnection(this);

        //初始化过程会开启该控制连接线程上的读写线程，也就是开始接受用户的命令，执行并返回结果给用户
        startReaderThread();
        startWriterThread();
    }


    /** Try to flush(冲洗) the content of the read array to the incoming buffer.
     * @return TRUE is flushed, FALSE otherwise
     */
    //将rarray中的字符加入到incomingBuffer中去
    protected boolean flushReadArray() throws Exception {
        if (!rarrayComplete) {
            return false;//如果数据没有读完，则不能进行转换
        }

        //Decode and feed collected bytes to the incoming buffer (also remove the UTF-8 magic number if present)
        //将array中的二进制流解码成相应的字符串，然后将不是魔数的字符加入到incomingBuffer中去
        String s = new String(rarray.toByteArray(), utf8 ? CHARSET_UTF8 : CHARSET_ASCII);
        char[] carr = s.toCharArray(); //将字符串转成char数组
        //如果含有魔数，则将其移出掉，将其它字符写入到incomingBuffer中
        synchronized (incomingBuffer) {
            for (char c : carr) {
                if (c != UTF8_MAGIC_NUMBER) {
                    incomingBuffer.append(c);
                }
            }
        }

        //Reset the read array
        //重置rarray数组
        rarray.reset();
        //重设rarrayComplete标志
        rarrayComplete = false;

        return true;
    }


    /**Decode(解码) and write the next part of user's input to the incoming buffer.
     * Used by the "read" routine to receive UFT-8 bytes.
     * @param arr Byte array containing the next part of user's input
     * @param stopIndex User's input stops at this index in the byte array
     */
    //将从SocketChannel中读取的byte[]数组转成字符加入到incomingBuffer中去
    protected void pushIncomingBuffer(byte[] arr, int stopIndex) throws Exception {
    /* We will feed user input to the read array one byte at a time till we hit any 1-byte character.
     * Only then the bytes in the read array may be safely decoded to UTF-8 (where one char may be
     * represented by 2-3 bytes) and written to the incoming buffer. For performance we will not flush
     * the read array every time we encounter 1-byte character, but we will do so before any 2-3 bytes
     * UTF-8 character and in the end if possible (rarrayComplete). If the read array cannot be decoded
     * then the bytes in it will wait till the next "read" routine(程序).
     */
        for (int z = 0; z < stopIndex; z++) {
            byte b = arr[z]; //Java中Byte是包装类，byte是基本类型，都表示8个bit

            //x00-x7F is a 1-byte character (UTF-8/ASCII), otherwise the character takes 2-3 bytes
            //x00-x7F的utf-8字符只需要一个字节表示(ASCII码字符)，否则就是需要几个字符表示的utf-8字符
            if (b >= 0 && b <= 127) {//表示是ASCII字符
                //Add the byte to the read array and proceed (read array may now be decoded)
                //将这个字符加入到ByteArrayOutputStream中去，同时设定rarrayComplete标志为true,表明rarray中的字符是完整的字符
                rarray.write(b);
                rarrayComplete = true;
            } else {//UTF-8字符，需要几个字符表示,每次加入这些字符之前，必须要将缓冲区进行清空
                //Try to flush the read array then add the byte to it and proceed
                flushReadArray();
                rarray.write(b);
            }
        }

        //Try to flush the read array one more time in the end
        //最后还需要将缓冲区进行清空
        flushReadArray();
    }


    //从SocketChannel中读取用户输入的数据

    /**读数据的基本逻辑是利用byteBuffer从该连接对应的SocketChannel中读取数据,
     然后再将byteArray中的字节流转换成字节数组，然后在将这个字节数组按照编码方式转成对应
     的字符写入到对应的StringBuffer中去*/
    protected void read() throws Exception {
    /* We must not read anything if:
     * 1. There is some data in outgoing buffer waiting to be send to the user
     * 2. User did not receive a welcome message yet and it is not yet in the outgoing buffer
     * 3. Connection is poisoned
     */
        /**在以下三种情况下不能读取数据
         1.输出缓冲区中还存在未向用户发送的数据
         2.用户没有接收到欢迎信息并且这个欢迎信息也没有在输出缓冲区中
         3.连接处于poisoned状态
         */
        if (getOutgoingBufferSize() > 0 || bytesWrote == 0 || poisoned) {
            Thread.sleep(sleep);
            return;
        }

        //Read data from socket and append it to the incoming buffer.

        rbuffer.clear();
        int i = sc.read(rbuffer); //Thread blocks here...,线程阻塞

        //Client disconnected?
        //没有读到任何东西，可能是客户端未连接
        if (i == -1) {
            throw new BrokenPipeException();
        }

        bytesRead += i;//统计读入的byte数
        log.debug("Read from socket " + i + " bytes (total " + bytesRead + ")");

        //This will add user input to the incoming buffer decoded with proper charset
        //读入的是buffer,然后转成byte[]数组，然后再利用ByteArrayOutPutStream将
        //这些字符再转成byteArray,然后解码成相应的字符类型，在写入输入缓冲区中去（不是很能理解这个过程，为什么不直接操作第一次转出来的Byte[]数组）
        //理解如下：我们从rbuffer中得到的byte数组中是可能同时含有ACSII码字符和UTF-8字符的，在这种情况下，没有办法进行一次性解码，所以我们要先将
        //这里面的各种字符分离开来，分别进行解码，这样才能得到正确的结果。
        byte[] barr = rbuffer.array();
        pushIncomingBuffer(barr, i);//将传入的byte[]数组转成字符加入到incomingBuffer中去

        //Execute commands waiting in the buffer
        //执行用户输入的命令
        executeCommands();
    }


    /** Encode(编码) and return the next part of server's response from the the outgoing buffer.
     * Used by the "write" routine to send UFT-8 bytes.
     * @param maxBytes Byte array size limit
     * @return Encoded byte array not longer than the limit or NULL if there is nothing to write
     */
    //将outgoingBuffer中的字符编码，然后写入到warray中，写给用户
    protected byte[] popOutgoingBuffer(int maxBytes) throws Exception {
        byte[] barr;
        if (warray.size() > 0) { //如果warray中还有数据，将这些数据进行写入

            //Reminder from the last output is still pending
            barr = warray.toByteArray();
            warray.reset();

        } else {

            //Get a string from the outgoing buffer to write into socket
            //从outgoingBuffer中获得指定长度的字符串，然后将这个字符串转换成byte数组，并返回
            //注意处理几种边界情况
            String str;
            int end = maxBytes;
            synchronized (outgoingBuffer) {
                //Correct sub-string length (if current length is longer than available data size)
                //如果指定的长度大于最大长度，则取outgoingBuffer的最大长度
                if (end > outgoingBuffer.length()) {
                    end = outgoingBuffer.length();
                }
                if (end == 0) {
                    return null; //Nothing to write
                }
                //获得对应的字符串
                str = outgoingBuffer.substring(0, end);
                //Remove this string from the outgoing buffer
                //将这一部分字符串从outgoingBuffer中移除
                outgoingBuffer.delete(0, end);
            }

            //Convert to byte array, the length of the byte array may be greater than the string length (UTF-8 encoding)
            //根据字符对应的编码，将字符串转成对应的byte数组，注意如果使用utf-8编码长度可能会增加
            barr = str.getBytes(utf8 ? CHARSET_UTF8 : CHARSET_ASCII);

        }

        //Will the new array fit into the buffer?
//如果编码后的字符长度大于wbuffer的长度，则将多余的部分放到warray中，下次再进行写入
        if (barr.length > maxBytes) {
            warray.write(barr, maxBytes, barr.length - maxBytes); //This will not fit into the socket buffer, save it for the next "write"
            byte[] trg = new byte[maxBytes];
            System.arraycopy(barr, 0, trg, 0, maxBytes);
            return trg;
        } else {
            return barr;
        }
    }


    //写入数据到SocketChannel中去　　　　
    protected void write() throws Exception {

        //Read more data from the outgoing buffer into the buffer only if the buffer is empty
        //如果wbuffer中没有数据了，从outgoingBuffer中编码一些字符写入其中
        if (!wbuffer.hasRemaining()) {

            //This will get server response from the outgoing buffer encoded with proper charset
            int cap = wbuffer.capacity();
            byte[] barr = popOutgoingBuffer(cap);

            //Nothing to write?
            if (barr == null) {
                Thread.sleep(sleep);
                return;
            }

            //Write out to the buffer
            //将buffer中的数据写入到wbuffer中
            wbuffer.clear();
            wbuffer.put(barr);
            wbuffer.flip();
        }

        //Forward the data to the use
        //将数据写给用户
        int i = sc.write(wbuffer); //Thread blocks here...

        //Client disconnected?
        //写入失败，客户端未连接
        if (i == -1) {
            throw new BrokenPipeException();
        }

        bytesWrote += i;
        log.debug("Wrote into socket " + i + " bytes (total " + bytesWrote + ")");
    }


    //用于判断处于poisoned状态下,能否立刻结束掉该控制连接线程
    //在以下几种情况下，该控制连接是不不能马上就结束掉的
    //1.如果这个控制连接对应的数据连接仍然处于active状态
    //2.如果欢迎信息还未发送给用户
    //3.马上要发送一个回应
    public void service() throws Exception {
    /* If connection has been poisoned then we can destroy it only when it writes all data out.
     * We cannot kill it while it has an active data connection.
     * We cannot kill if it did not write a welcome message yet.
     * We cannot kill if it is expecting a reply to be added soon (poisoned byte marker)
     */
        if (poisoned) {
            boolean kill = true;

            if (dataConnection != null && !dataConnection.isDestroyed()) {
                kill = false; //Active data connection
            }

            if (getOutgoingBufferSize() > 0) {
                kill = false; //Data is waiting to be sent
            }

            if (bytesWrote == 0) {
                kill = false; //Welcome message is expected
            }

            Long markerBytesWrote = (Long) session.getAttribute(SessionAttributeName.BYTE_MARKER_POISONED);
            if (markerBytesWrote != null && bytesWrote <= markerBytesWrote) {
                kill = false; //A reply is expected
            }

            if (kill) {
                throw new PoisonedException();
            }
        }
    }


    /** Execute commands waiting in the incoming buffer */
    protected void executeCommands() throws Exception {
        while (true) {
            Command command = getNextCommand();
            if (command == null) {
                break;
            }
            commandProcessor.execute(command);
        }
    }


    /** Reads next user command from the incoming buffer
     * @return Command or NULL if it's not ready yet
     */
    protected Command getNextCommand() throws Exception {
        //Extract the next command from buffer
        String input;
        synchronized (incomingBuffer) {
            int i = incomingBuffer.indexOf("\r\n");//\r\n作为每条命令的结束符
            if (i == -1) {
                return null;                    //Nothing to extraxt yet (the line is not finished)
            }
            input = incomingBuffer.substring(0, i);
            incomingBuffer.delete(0, i + 2);               //Also delete \r\n in the end of the command
            if (input.trim().length() == 0) {
                return null; //This is an empty string, skip it
            }
            log.debug("Extracted user input: " + input);
        }

        Command command = commandFactory.create(input);//创建对应的命令对象
        command.setConnection(this);

        //If INTERRUPT state is set then ignore all but special FTP commands (same for the poisoned).
        if (interruptState && !command.processInInterruptState()) {
            log.debug("Execution of the command is not allowed while the connection is in INTERRUPT state (dropping command)");
            return null;
        }
        if (poisoned && !command.processInInterruptState()) {
            log.debug("Execution of the command is not allowed while the connection is poisoned (dropping command)");
            return null;
        }

        return command;
    }


    public synchronized void reply(Reply reply) {
        //Prepare reply and write it out
        String prepared = reply.prepare();
        synchronized (outgoingBuffer) {
            outgoingBuffer.append(prepared);
        }
        log.debug("Prepared reply: " + prepared.trim());

    /* Change "interrupt" state: if code starts with "1" then set it, otherwise unset.
     * FTP spec: all codes that start with 1 demand client to wait for another reply.
     */
        if (reply.getCode().startsWith("1")) {
            interruptState = true;
            log.debug("Reply has triggered INTERRUPT state");
        } else if (interruptState) {
            //Check if reply's command can clear INTERRUPT state, otherwise leave the state as it is
            Command command = reply.getCommand();
            if (command == null || command.canClearInterruptState()) {
                interruptState = false;
                log.debug("Reply has cleared INTERRUPT state");
            }
        }
    }


    public Session getSession() {
        return session;
    }


    public DataConnection getDataConnection() {
        return dataConnection;
    }


    public void setDataConnection(DataConnection dataConnection) {
        this.dataConnection = dataConnection;
    }


    public synchronized void destroy() {
        //Abort data connection initiator if active
        if (dataConnectionInitiator.isActive()) {
            dataConnectionInitiator.abort();
        }

        //Destory data connection if exists
        if (dataConnection != null) {
            dataConnection.destroy();
        }

        //Test if there is data channel left in the session
        closeSessionDataChannel();

        super.destroy();
    }


    /** Close a data channel if exists in the session */
    protected void closeSessionDataChannel() {
        Channel odc = (Channel) session.getAttribute(SessionAttributeName.DATA_CONNECTION_CHANNEL);
        if (odc != null) {
            log.debug("Attempting to close data channel in session");
            session.removeAttribute(SessionAttributeName.DATA_CONNECTION_CHANNEL);
            try {
                odc.close();
            } catch (Throwable e) {
                log.error("Error closing data channel (ignoring)", e);
            }
        }
    }


    public DataConnectionInitiator getDataConnectionInitiator() {
        return dataConnectionInitiator;
    }


    public int getOutgoingBufferSize() {
        synchronized (outgoingBuffer) {
            return outgoingBuffer.length();
        }
    }


    public int getIncomingBufferSize() {
        synchronized (incomingBuffer) {
            return incomingBuffer.length();
        }
    }


    public boolean isUtf8() {
        return utf8;
    }


    public void setUtf8(boolean utf8) {
        this.utf8 = utf8;
    }
}
