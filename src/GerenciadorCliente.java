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
        InetAddress ipGerenciadorServidor = InetAddress.getByName("25.255.255.255");
        InetAddress ipServidor = null;

        System.out.println("Iniciando Aplicação Cliente!");

        String msg = "::Quem está disponível?";
        byte[] requisicaoComunicacaoServidor = msg.getBytes();
        DatagramPacket envelopeAEnviar = new DatagramPacket(requisicaoComunicacaoServidor, requisicaoComunicacaoServidor.length, ipGerenciadorServidor, 5000);
        socket.send(envelopeAEnviar);
        System.out.println("Conexão Concluida");

        byte[] respostaRequisicao = new byte[1024];
        DatagramPacket envelopeAReceber = new DatagramPacket(respostaRequisicao, respostaRequisicao.length);
        socket.receive(envelopeAReceber);
        String msgRecebida = new String(envelopeAReceber.getData()).trim();

        if(msgRecebida.contains("::")) {
            ipServidor = InetAddress.getByName(envelopeAReceber.getAddress().getHostAddress());
        }

        while(true) {
            byte[] respostaDoServidor = new byte[1024];
            System.out.println("Resposta do Servidor: ");
            DatagramPacket envelopeAReceberServidor = new DatagramPacket(respostaDoServidor, respostaDoServidor.length);
            socket.receive(envelopeAReceberServidor);
            String msgRecebidaServidor = new String(envelopeAReceberServidor.getData()).trim();
            System.out.println(msgRecebidaServidor);
            InetAddress ipUsuario = null;

            if(msgRecebida.charAt(0) == 'i' && msgRecebida.charAt(1) == 'p'){
                String[] ipUsuarioSplited = msgRecebida.split(":");
                String ipUsuarioPosi1 = ipUsuarioSplited[1].trim();
                ipUsuario = InetAddress.getByName(ipUsuarioPosi1);
                iniciarConversacao(teclado, socket, ipUsuario, ipServidor, envelopeAReceber);
                continue;
            }

            //Enviando resposta ao servidor
            String resposta = null;
            resposta = teclado.nextLine();
            byte[] respostaAoServidor;
            respostaAoServidor = resposta.getBytes();
            DatagramPacket envelopeResposta = new DatagramPacket(respostaAoServidor, respostaAoServidor.length, ipServidor, 5000);
            socket.send(envelopeResposta);

            if (resposta.equalsIgnoreCase("x")) {
                encerrarConexao(socket);
                System.out.println("Cliente desregistrado!");
                break;
            } else if(resposta.equals("0")) {
                System.out.println("Aguardando um usuário iniciar conversaçao...");
                respostaDoServidor = new byte[200];
                envelopeAReceber = new DatagramPacket(respostaDoServidor, respostaDoServidor.length);
                socket.receive(envelopeAReceber);
                String msgChat = new String(envelopeAReceber.getData()).trim();
                System.out.println("Mensagem recebida de: " + envelopeAReceber.getAddress().getHostAddress() + " | " + envelopeAReceber.getAddress().getHostName() + ": " + msgChat);
                System.out.println("(Digite [FIM] para encerrar o chat)");
                ipUsuario = InetAddress.getByName(envelopeAReceber.getAddress().getHostAddress());
                iniciarConversacao(teclado, socket, ipUsuario, ipServidor, envelopeAReceber);
            }
        }
    }

    public static void encerrarConexao(DatagramSocket socket){
        socket.close();
        System.out.println("Conexão encerrada");
    }

    public static void iniciarConversacao(Scanner teclado, DatagramSocket socket, InetAddress ipUsuario,  InetAddress ipServidor, DatagramPacket dp) throws IOException {
        String resposta;

        while(true){
            System.out.println(InetAddress.getLocalHost().getHostAddress() + " | " + InetAddress.getLocalHost().getHostName());
            resposta = teclado.nextLine();
            byte[] respostaAoCliente= new byte[100];
            respostaAoCliente = resposta.getBytes();
            DatagramPacket envelopeResposta = new DatagramPacket(respostaAoCliente, respostaAoCliente.length, ipUsuario, 8090);
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
        DatagramPacket envelopeAEnviar = new DatagramPacket(requisicaoServidor, requisicaoServidor.length, ipServidor, 5000);
        socket.send(envelopeAEnviar);
    }
}
