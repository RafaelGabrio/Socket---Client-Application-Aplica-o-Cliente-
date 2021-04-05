import java.io.IOException;
import java.net.*;
import java.util.*;

public class GerenciadorCliente {

    public static void main(String args[]) throws IOException {
        /* porta 5000 para comunicação cliente - servidor
        porta 8090 para comunicação cliente - cliente */

        Scanner teclado = new Scanner(System.in);

        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        InetAddress ipServidor = InetAddress.getByName("255.255.255.255");

        System.out.println("Iniciando Aplicação Cliente!");

        String msg = "::Quem está disponível?";
        byte[] requisicaoServidor = msg.getBytes();
        DatagramPacket envelopeAEnviar = new DatagramPacket(requisicaoServidor, requisicaoServidor.length, ipServidor, 5000);
        socket.send(envelopeAEnviar);
        System.out.println("Conexão Concluida");

        //Solicita registro em um servidor disponível
        solicitarRegistroEmServidor(socket, ipServidor);

        while(true) {
            byte[] respostaDoServidor = new byte[1024];
            System.out.println("Resposta do Servidor: ");
            DatagramPacket envelopeAReceber = new DatagramPacket(respostaDoServidor, respostaDoServidor.length);
            socket.receive(envelopeAReceber);
            String msgRecebida = new String(envelopeAReceber.getData()).trim();
            System.out.println(msgRecebida);
            InetAddress ipUsuario = null;

            if(msgRecebida.charAt(0) == 'i' && msgRecebida.charAt(1) == 'p'){
                String[] ipUsuarioSplited = msgRecebida.split(":");
                String ipUsuarioPosi1 = ipUsuarioSplited[1].trim();
                ipUsuario = InetAddress.getByName(ipUsuarioPosi1);
                iniciarConversacao(teclado, socket, ipUsuario, envelopeAReceber);
                continue;
            }

            //Enviando resposta ao servidor
            String resposta = null;
            resposta = teclado.nextLine();
            byte[] respostaAoServidor;
            respostaAoServidor = resposta.getBytes();
            DatagramPacket envelopeResposta = new DatagramPacket(respostaAoServidor, respostaAoServidor.length, ipServidor, 5000);
            socket.send(envelopeResposta);

            if (resposta.equalsIgnoreCase("::x")) {
                encerrarConexao(socket);
                System.out.println("Cliente deslogado!");
                break;
            } else if(resposta.equals("::0")) {
                System.out.println("Aguardando um usuário niciar conversaçao...");
                respostaDoServidor = new byte[200];
                envelopeAReceber = new DatagramPacket(respostaDoServidor, respostaDoServidor.length);
                socket.receive(envelopeAReceber);
                String msgChat = new String(envelopeAReceber.getData()).trim();
                System.out.println("Mensagem recebida de: " + envelopeAReceber.getAddress().getHostAddress() + " | " + envelopeAReceber.getAddress().getHostName() + ": " + msgChat);
                System.out.println("(Digite [FIM] para encerrar o chat)");
                iniciarConversacao(teclado, socket, ipUsuario, envelopeAReceber);
            }
        }
    }

    public static void solicitarRegistroEmServidor(DatagramSocket socket, InetAddress ip) throws IOException {
        /**byte[] respostaDoServidor = new byte[1024];
        System.out.println("Servidores Disponíveis: ");
        DatagramPacket envelopeAReceber = new DatagramPacket(respostaDoServidor, respostaDoServidor.length);
        socket.receive(envelopeAReceber);
        String msgRecebida = new String(envelopeAReceber.getData()).trim();
        System.out.println(msgRecebida);**/

        Random random = new Random();
        int indice = random.nextInt(servidoresOnline().size());
        var servidorSelecionado = servidoresOnline().get(indice);



        /**System.out.println("Enviando solicitação de registro!");
        byte[] requisicaoRegistro = new byte[100];
        DatagramPacket envelopeAEnviar = new DatagramPacket(requisicaoRegistro, requisicaoRegistro.length, ip, 5000);
        socket.send(envelopeAEnviar);
        System.out.println("Registro concluído!");**/

    }

    public static void encerrarConexao(DatagramSocket socket){
        socket.close();
        System.out.println("Conexão encerrada");
    }

    public static void iniciarConversacao(Scanner teclado, DatagramSocket socket, InetAddress ip, DatagramPacket dp) throws IOException {
        String resposta;

        while(true){
            System.out.println(InetAddress.getLocalHost().getHostAddress() + " | " + InetAddress.getLocalHost().getHostName());
            resposta = teclado.nextLine();
            byte[] respostaAoCliente= new byte[100];
            respostaAoCliente = resposta.getBytes();
            DatagramPacket envelopeResposta = new DatagramPacket(respostaAoCliente, respostaAoCliente.length, ip, 8090);
            System.out.println("Enviando mensagem para o usuário: " + dp.getAddress().getHostName());
            socket.send(envelopeResposta);

            if(resposta.equalsIgnoreCase("fim")){
                break;
            }

            byte[] respostaDoUsuario = new byte[1024];
            dp = new DatagramPacket(respostaDoUsuario, respostaDoUsuario.length);
            socket.receive(dp);
            String msgChat = new String(dp.getData());
            System.out.println("Mensagem recebida de: " + dp.getAddress().getHostAddress() + " | " + dp.getAddress().getHostName() + ": " + msgChat);
        }

        System.out.println("Encerrando chat...");
        String msg = "fim";
        byte[] requisicaoServidor = new byte[200];
        requisicaoServidor = msg.getBytes();
        DatagramPacket envelopeAEnviar = new DatagramPacket(requisicaoServidor, requisicaoServidor.length, InetAddress.getByName("25.74.5.8"), 5000);
        socket.send(envelopeAEnviar);
    }

    public static List<InetAddress> servidoresOnline() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }
}
