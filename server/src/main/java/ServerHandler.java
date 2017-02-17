
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by cahya on 16/02/17.
 */
public class ServerHandler {
    private InetSocketAddress address;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    Iterator keys;

    public ServerHandler(String address, int port){
        this.address=new InetSocketAddress(address, port);
    }

    public void startServer() throws IOException{
        selector=Selector.open();
        serverChannel=ServerSocketChannel.open();

        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(address);

        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        while(true){
            this.selector.select();

            keys=selector.selectedKeys().iterator();
            while (keys.hasNext()){
                SelectionKey key=(SelectionKey)keys.next();

                keys.remove();

                if(!key.isValid()){
                    continue;
                }else if(key.isAcceptable()){
                    accept(key);
                }else if(key.isReadable()){
                    read(key);
                }
            }

        }
    }

    private void accept(SelectionKey key) throws IOException{
        ServerSocketChannel serverSocket=(ServerSocketChannel)key.channel();
        SocketChannel clientChannel=serverSocket.accept();
        clientChannel.configureBlocking(false);
        Socket clientSocket=clientChannel.socket();
        SocketAddress clientAddress=clientSocket.getRemoteSocketAddress();
        System.out.println("Connected to: "+clientAddress);

        clientChannel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException{
        SocketChannel clientChannel=(SocketChannel)key.channel();
        ByteBuffer buffer=ByteBuffer.allocate(1024);
        int numRead=-1;
        numRead=clientChannel.read(buffer);

        if(numRead==-1){
            Socket clientSocket=clientChannel.socket();
            SocketAddress clientAddress=clientSocket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: "+clientAddress);
            clientChannel.close();
            key.cancel();
            return;
        }

        byte[] data=new byte[numRead];
        System.arraycopy(buffer.array(),0,data,0,numRead);
        System.out.println("Got: "+new String(data));
        broadcast(new String(data));
    }

    private void broadcast(String message) throws IOException{
        ByteBuffer buffer=ByteBuffer.wrap(message.getBytes());
        System.out.println("System sending: "+message);
        for(SelectionKey key:selector.keys()){
            if(key.isValid()&&key.channel() instanceof SocketChannel){
                SocketChannel clientChannel=(SocketChannel) key.channel();
                clientChannel.write(buffer);
                buffer.rewind();
            }
        }
    }
}
