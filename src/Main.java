public class Main {
    private static void help() {
        System.out.println("Usage:");
        System.out.println("    -m <mode>| --mode <mode>   required parameter. <mode>=[server|client]");
        System.out.println("    -h <host>| --host <host>   required parameter for client mode. The server host name or IP.");
        System.out.println("    -n <name>| --name <name>   required parameter for client mode. The client name.");
        System.exit(0);
    }

    public static void main(String[] args) {
        if (args.length == 0) help();
        String mode = null;
        String host = null;
        String name = null;
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-m") || args[i].equals("-mode")) {
                    mode = args[i + 1];

                }
                if (args[i].equals("-h") || args[i].equals("-host")) {
                    host = args[i + 1];

                }
                if (args[i].equals("-n") || args[i].equals("-name")) {
                    name = args[i + 1];

                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            mode = null;
            host = null;
            name = null;
        }
        if (mode == null) {
            help();
        }else {
            switch (mode) {
                case "server":
                    server.JFXServer.main(null);
                    break;
                case "client":
                    if (host==null || name == null) help();
                    client.JFXClient.main(new String[]{host, name});
                    break;
                default:
                    help();
            }
        }


    }
}
