import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServidorUDP {
    static List<DatagramPacket> clientes = new ArrayList<>();
    static DatagramSocket socket;

    public static void main(String[] args) throws IOException {
        Scanner teclado = new Scanner(System.in);
        MulticastSocket grupoMulticast = null;
        InetAddress ipGrupo = null;
        int portaMulticast;

        System.out.println("Qual a porta para a comunicação com os clientes?");
        int portaClientes = teclado.nextInt();
        socket = new DatagramSocket(portaClientes);

        System.out.println("Qual a porta para a comunicação no grupo multicast?");
        portaMulticast = teclado.nextInt();

        try {
            grupoMulticast = new MulticastSocket(portaMulticast);
            ipGrupo = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
            grupoMulticast.joinGroup(ipGrupo);
        } catch (SocketException e) {}

        System.out.println("Digite: finalizar, para parar a aplicação");
        String msgTeclado = teclado.nextLine();

        String msg;
        String msgRecebida;

        while (!msgTeclado.equalsIgnoreCase("finalizar")){
            //recebendo uma mensagem do cliente
            byte[] cartaAReceber = new byte[1000];
            DatagramPacket envelopeAReceber = new DatagramPacket(cartaAReceber, cartaAReceber.length);
            socket.receive(envelopeAReceber);
            msgRecebida = new String(envelopeAReceber.getData()).trim();

            if(msgRecebida.contains("::")) {
                enviarMensagem("::Estou disponível", envelopeAReceber);
                System.out.println(msgRecebida);
            } else if(msgRecebida.contains(":!")) {
                atualizarClientes(msgRecebida);
            } else {

                if (msgRecebida.equalsIgnoreCase("x")) {
                    removerDaLista(envelopeAReceber.getAddress());
                }

                if (estaNaLista(envelopeAReceber.getAddress()) == false) {
                    clientes.add(envelopeAReceber);
                    System.out.println("Cliente conectado");
                    String mensagem = ":!" + clientes.toString();
                    enviarParaGrupo(mensagem, grupoMulticast, ipGrupo, portaMulticast);
                }

                if (eInteiro(msgRecebida)) {
                    if (!msgRecebida.equalsIgnoreCase("0")) {
                        //enviando ip cliente para comunicação direta
                        msg = "ip:" + clientes.get(Integer.parseInt(msgRecebida) - 1).getAddress().getHostAddress();
                        enviarMensagem(msg, envelopeAReceber);
                    }
                }

                if (msgRecebida.equalsIgnoreCase("fim") || msgRecebida.equalsIgnoreCase("")) {
                    //enviando lista de clientes
                    String lista;
                    lista = "{";
                    int id = 1;
                    for (int i = 0; i < clientes.size(); i++) {
                        lista += id + i + "= [Usuário: " + clientes.get(i).getAddress().getHostName()
                                + " | IP: " + clientes.get(i).getAddress().getHostAddress()
                                + " | Porta: " + clientes.get(i).getPort() + "], ";
                    }
                    lista += "}";
                    msg = "Esta é a lista de clientes: " + lista + "\n Digite o número de algum deles caso deseje se comunicar, \n Digite 0 para aguardar comunicação, ou \n Digite x para finalizar aplicação.";
                    enviarMensagem(msg, envelopeAReceber);
                }
                if (msgRecebida.equalsIgnoreCase("x")) {
                    removerDaLista(envelopeAReceber.getAddress());
                }
            }
        }
        grupoMulticast.leaveGroup(InetAddress.getLocalHost());
    }

    private static void atualizarClientes(String msgClientes) {
        clientes = null;
        int index = 0;
        for (int i = 0; i < msgClientes.length(); i++) {
            char c = msgClientes.charAt(i);
            if(c == '['){
                index = i;
            }
            if(c == ']'){
                String sequencia =  msgClientes.substring(index, i);
                DatagramPacket novoCliente = new DatagramPacket(sequencia.getBytes(), sequencia.length());
                clientes.add(novoCliente);
            }
        }
    }

    public static boolean eInteiro(String s){
        boolean inteiro = true;

        for ( int i = 0; i < s.length(); i++ ) {
            if ( !Character.isDigit( s.charAt(i) ) ) {
                inteiro = false;
                break;
            }
        }
        return inteiro;
    }

    public static void enviarMensagem(String msg, DatagramPacket envelopeAReceber) throws IOException {
        byte[] cartaAEnviar = new byte[1000];
        cartaAEnviar = msg.getBytes();
        DatagramPacket envelopeAEnviar = new DatagramPacket(cartaAEnviar, cartaAEnviar.length, envelopeAReceber.getAddress(), envelopeAReceber.getPort());
        socket.send(envelopeAEnviar);
    }

    public static void enviarParaGrupo(String msg, MulticastSocket grupoMulticast, InetAddress ipGrupo, int portaMulticast){
        DatagramPacket dtgrm = new DatagramPacket(msg.getBytes(),
                msg.length(), ipGrupo, portaMulticast);
        try {
            grupoMulticast.send(dtgrm);
        } catch (IOException e) { }
    }

    public static boolean estaNaLista(InetAddress enderecoCliente){
        boolean esta = false;
        for (int i = 0; i < clientes.size(); i++) {
            if(clientes.get(i).getAddress().equals(enderecoCliente)){
                esta = true;
            }
        }
        return esta;
    }

    public static void removerDaLista(InetAddress enderecoCliente){
        for (int i = 0; i < clientes.size(); i++) {
            if(clientes.get(i).getAddress().equals(enderecoCliente)){
                clientes.remove(clientes.get(i));
            }
        }
    }
}