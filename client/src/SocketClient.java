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

public class SocketClient {

    public void startClient() throws IOException, InterruptedException {

        InetSocketAddress hostAddress = new InetSocketAddress("127.0.0.1", 3345);
        SocketChannel client = SocketChannel.open(hostAddress);

        System.out.println("CTFClient started");

        // Send messages to server
        String [] messages = new String [3];

        messages[0] = "1F000000399306130201280101000000040008002800070005001200000000439600060037422005******4206=****201*************00090016422005******420600140002#8002000200000000000000000005700290002120032000820190220004200020500450016422005******4206004600021600480026**************************005300010005600020300590001000650002000072002016165PP30760960     0078000301500790014201902201705170118000205013200020101330002030134000307601430001001470005C021201730001001860001101960001902000007C0302000207000200022200020002230020INGENICO            02410013002.10 1412050252000100274001604.21 150305    028100041.080302000624093003100016VISACREDITO     03120005L060203190019iPP320             03270005C013803420001003840003D04038500203806840746612620420404040002000405001000000000000408000800000000<0><0><0><0>";
        messages[1] = "12000000399306130201290101000000040008002800070005001200000000439600060037422005******4206=****201*************000800020100090016422005******420600140002#800200020000000000000000000570029001000000000000032000820190220004200020500450016422005******4206004600021600480026**************************005300010005600020300590001000600001300650002000071016682025C009F2701809F2608B8079CB9FBC916BC9F3602003E950500800080009F34034103029F37045849E8D59F3303E0E0C05F280200769F100706010A03A498009A031902205F3401004F07A00000000310100072002016165PP30760960     0078000303000790014201902201705250118000205012200011012300012013200020101330002030134000307601350004000101430001001450047002.11 141205108a01T                           01470005C02120170000100173000100176000201018600011019000020001960001902000007C0302000204000150207000200021900010022200020002230020INGENICO            02410013002.10 141205024500020102460002010252000100273000130274001604.21 150305    028100041.080302000624093003030001103040001103100016VISACREDITO     03120005L060203190019iPP320             03270005C013803420001003840003D04038500203806840746612620420404040002000405001000000000000408000800000000<0><0><0><0><0><0><0><0>";
        messages[2] = "8000000039930613020131010100000004000800300007003200082019022000420002000053000100072002016165PP30760960     00780003015007900142019022017064901470005C021202000007C030200020700020002520001003120005L060203270005C013803840003D0404040002000405001000000000000408000800000000";

        for (int i = 0; i < messages.length; i++) {
            byte [] message = new String(messages [i]).getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(message);
            client.write(buffer);
            System.out.println("Requisicao ao CTFServer: " + messages [i]);
            this.readAnswer(client);
            buffer.clear();
//            Thread.sleep(20000);
        }

    }

    private void accept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);

        // register channel with selector for further IO
        channel.register(selector, SelectionKey.OP_READ);
    }

    private boolean read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(9999);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);

            channel.close();
            key.cancel();
            return false;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        buffer.clear();
        System.out.println("Resposta do CTF Server: " + new String(data));
        return false;

    }

    private boolean readAnswer(SocketChannel client) throws IOException {

        Selector selector = Selector.open();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);

        while (true) {
            // wait for events
            selector.select();

            //work on selected keys
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();

                keys.remove();

                if (!key.isValid()) {
                    break;
                }

                if (key.isAcceptable()) {
                    this.accept(key, selector);
                } else if (key.isReadable()) {
                    return this.read(key);
                } else {
                    client.close();
                }
            }
        }

    }

}