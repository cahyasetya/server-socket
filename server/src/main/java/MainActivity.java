import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by cahya on 16/02/17.
 */
public class MainActivity {
    public static void main(String[] args) {
        Runnable runnable=new Runnable() {
            public void run() {
                try {
                    new ServerHandler("localhost", 8888).startServer();
                }catch(IOException ioException){
                    ioException.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();
    }
}
