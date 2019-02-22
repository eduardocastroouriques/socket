import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class SocketServer {
	private Selector selector;
    private Map<SocketChannel,List<byte[]>> dataMapper;
    private InetSocketAddress listenAddress;
    
    public static void main(String[] args) throws Exception {
    	Runnable server = new Runnable() {
			@Override
			public void run() {
				 try {
					new SocketServer("127.0.0.1", 3345).startServer();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};

       new Thread(server).start();
    }

    public SocketServer(String address, int port) throws IOException {
    	listenAddress = new InetSocketAddress(address, port);
        dataMapper = new HashMap<SocketChannel,List<byte[]>>();
    }

    // create server channel	
    private void startServer() throws IOException {
        this.selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("CTF Server started...");

        while (true) {
            // wait for events
            this.selector.select();

            //work on selected keys
            Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();

                // this is necessary to prevent the same key from coming up 
                // again the next time around.
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    this.accept(key);
                }
                else if (key.isReadable()) {
                    this.read(key);
                } else if (key.isWritable()){
                    System.out.println("WARNING: CTFServer does not implement writable");
                }
            }
        }
    }

    //accept a connection made to this channel's socket
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);

        // register channel with selector for further IO
        dataMapper.put(channel, new ArrayList<byte[]>());
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void write(SelectionKey key, byte[] request) throws IOException {

        String operacao = OperacaoUtil.get(new String(request));
        String response = null;

        if(operacao.equals(Operacao.UMF.toString())){
            response = new UmfService().getSucesso();
        } else if(operacao.equals(Operacao.CREDITO.toString())){
            response = new CreditoService().getSucesso();
        } else if(operacao.equals(Operacao.CONFIRMACAO.toString())){
            response = new ConfirmacaoService().getSucesso();
        }

        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
//        buffer.clear();

        buffer.put(response.getBytes());
        buffer.flip();

        //while (buffer.hasRemaining()){
            System.out.println("Resposta ao CTFClient: " + new String(response));
            SocketChannel channel = (SocketChannel) key.channel();
            channel.write(buffer);
        //}
        buffer.clear();
    }

    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(9999);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            this.dataMapper.remove(channel);
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);

            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        System.out.println("Requesicao do CTFClient: " + new String(data));

        this.write(key, data);

    }
}