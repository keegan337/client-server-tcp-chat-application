package GUI;

import Client.Client;

import javax.swing.*;
import java.awt.*;

class LoginMenu {
    public static void main(String[] args) {
        Client client;
        if(args.length>0){
            client = new Client(args[0], args[1]);
        }else{
            client = new Client();
        }
        
        //Create new client
        client.start();


        //Create Frame and set layout
        JFrame frame = new JFrame("Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 180);
        frame.getContentPane().setLayout(new FlowLayout());

        //Create Text Fields
        JTextField userNameTextField = new JTextField("Username", 10);
        JTextField passwordTextField = new JTextField("Password", 10);
        frame.getContentPane().add(userNameTextField);
        frame.getContentPane().add(passwordTextField);

        //Create Login Button
        JButton loginButton = new JButton("LOGIN");
        frame.getContentPane().add(loginButton); // Adds Button to content pane of frame
        frame.setVisible(true);

        //On button click get text from text fields and verify user
        loginButton.addActionListener(e -> {
            System.out.println("Login clicked");
            String username = userNameTextField.getText();
            String password = passwordTextField.getText();
            if(client.login(username, password)) {
                MainMenu menu = new MainMenu(client);
                Client.username = username;
                frame.dispose();
                menu.setVisible(true);
                client.createThreads();
            }
            else{
                JOptionPane.showMessageDialog(null, "Incorrect username or password.");
            }
        });



    }
}
