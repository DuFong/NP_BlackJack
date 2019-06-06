import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
 
import javax.net.ssl.SSLSocket;
 
public class ThreadServer extends Thread {
 
    private SSLSocket socket;
    private InputStream input;
    private InputStreamReader reader;
    private BufferedReader br;
    private Server server;
 
    public ThreadServer(SSLSocket socket, Server server ){
        this.socket = socket;
        this.server = server;
    }
 
    @Override
    public void run() {
         
        try{
            String fromClient = null;
            input = socket.getInputStream();
            reader = new InputStreamReader(input);
            br = new BufferedReader(reader);
            
            while((fromClient = br.readLine())!=null){
                server.whisper(fromClient);
            }
             
        }catch(Exception e){
        }
         
    }
     
}
