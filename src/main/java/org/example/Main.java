package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Corretora SHULAMBS S.A.D \n");
        Broker broker = new Broker();

        try (Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("Selecione uma das opções abaixo: ");
                System.out.println("1 - Comprar ações");
                System.out.println("2 - Vender ações");
                System.out.println("3 - Informações sobre ações");
                System.out.println("0 - Sair");
                System.out.print(": ");
                String option = scanner.next();

                switch (option) {
                    case "1" -> {
                        System.out.print("\nDigite a quantidade de ações: ");
                        int quantity = scanner.nextInt();
                        System.out.print("Digite o preço: ");
                        double price = scanner.nextDouble();
                        System.out.print("Digite o código da ação: ");
                        String stockSymbol = scanner.next();
                        broker.sendBuyOrder(quantity + " " + price + " " + stockSymbol);
                        System.out.println("Pedido de compra realizada com sucesso! \n");
                    }
                    case "2" -> {
                        System.out.print("Digite a quantidade de ações: ");
                        int quantity = scanner.nextInt();
                        System.out.print("Digite o preço: ");
                        double price = scanner.nextDouble();
                        System.out.print("Digite o código da ação: ");
                        String stockSymbol = scanner.next();
                        broker.sendSellOrder(quantity + " " + price + " " + stockSymbol);
                        System.out.println("Pedido de venda realizada com sucesso! \n");
                    }
                    case "3" -> {
                        System.out.println("Digite o codigo de uma ação: ");
                        String stockSymbol = scanner.next();
                    }
                    case "0" -> System.exit(0);
                    default -> System.out.println("Opção inválida");
                }

            }
        }

    }
}