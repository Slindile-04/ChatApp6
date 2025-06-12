// I hope my lecture likes this version
package chatapp6;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class User {
    String name;
    String phone;
    String username;
    String password;

    public User(String name, String phone, String username, String password) {
        this.name = name;
        this.phone = phone.startsWith("0") ? "+27" + phone.substring(1) : phone;
        this.username = username;
        this.password = password;
    }

    public String getDisplayName() {
        return name + " {" + phone + "}";
    }
}

class Message {
    String id;
    String hash;
    String sender;
    String receiver;
    String content;
    boolean isRead;
    String timestamp;

    public Message(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.isRead = false;

        this.id = String.valueOf(new Random().nextInt(900000000) + 100000000); // 9-digit + 1 digit
        this.hash = generateHash();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd MMM yyyy");
        this.timestamp = now.format(formatter);
    }

    private String generateHash() {
        String[] words = content.trim().split("\\s+");
        String firstWord = words.length > 0 ? words[0] : "";
        String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;
        return (id.substring(0, 2) + ":" + firstWord + lastWord).toUpperCase();
    }

    public void markAsRead() {
        isRead = true;
    }

    public String getStatus() {
        return isRead ? "✓✓ Read" : "✓ Sent";
    }

    public void displayMessage() {
        String messageInfo = "[" + timestamp + "]\n"
                + sender + ": " + content + "\n"
                + "Status: " + getStatus() + "\n"
                + "ID: " + id + " | Hash: " + hash;
        JOptionPane.showMessageDialog(null, messageInfo);
    }
}

public class ChatApp6 {
    public static ArrayList<Message> chatHistory = new ArrayList<>();
    public static ArrayList<User> users = new ArrayList<>();
    public static final String USERS_FILE = "users.json";
    public static final String MESSAGES_FILE = "messages.json";
    static Gson gson = new Gson();

    public static void loadUsers() {
        try (Reader reader = new FileReader(USERS_FILE)) {
            Type listType = new TypeToken<ArrayList<User>>() {}.getType();
            users = gson.fromJson(reader, listType);
        } catch (IOException e) {
            users = new ArrayList<>();
        }
    }

    public static void saveUsers() {
        try (Writer writer = new FileWriter(USERS_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "⚠️ Failed to save users.");
        }
    }

    public static void loadMessages() {
        try (Reader reader = new FileReader(MESSAGES_FILE)) {
            Type listType = new TypeToken<ArrayList<Message>>() {}.getType();
            chatHistory = gson.fromJson(reader, listType);
        } catch (IOException e) {
            chatHistory = new ArrayList<>();
        }
    }

    public static void saveMessages() {
        try (Writer writer = new FileWriter(MESSAGES_FILE)) {
            gson.toJson(chatHistory, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "⚠️ Failed to save messages.");
        }
    }

    public static User registerNewUser() {
        String name = JOptionPane.showInputDialog("Enter your name:");
        if (name == null || name.equalsIgnoreCase("exit")) return null;

        String phone = JOptionPane.showInputDialog("Enter your phone number (starts with 0):");
        if (phone == null || phone.equalsIgnoreCase("exit")) return null;

        String username = name.toLowerCase().substring(0, Math.min(3, name.length())) + "_" + UUID.randomUUID().toString().substring(0, 2);
        String password = "Pwd@" + UUID.randomUUID().toString().substring(0, 4);

        User newUser = new User(name, phone, username, password);
        users.add(newUser);
        saveUsers();

        JOptionPane.showMessageDialog(null,
                "✅ Registration successful!\nUsername: " + username + "\nPassword: " + password);
        return newUser;
    }

    public static User loginUser() {
        while (true) {
            String username = JOptionPane.showInputDialog("🔐 Login\n\nUsername:");
            if (username == null || username.equalsIgnoreCase("exit")) return null;

            String password = JOptionPane.showInputDialog("Password:");
            if (password == null || password.equalsIgnoreCase("exit")) return null;

            for (User user : users) {
                if (user.username.equals(username) && user.password.equals(password)) {
                    JOptionPane.showMessageDialog(null, "✅ Welcome " + user.name + "!");
                    return user;
                }
            }

            JOptionPane.showMessageDialog(null, "❌ Incorrect username or password. Try again.");
        }
    }

    public static void sendMessage(User sender) {
        while (true) {
            ArrayList<String> options = new ArrayList<>();
            for (User user : users) {
                if (!user.equals(sender)) {
                    options.add(user.getDisplayName());
                }
            }
            options.add("Exit");

            String choice = (String) JOptionPane.showInputDialog(null,
                    "Who would you like to message?",
                    "Send Message",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options.toArray(),
                    null);

            if (choice == null || choice.equalsIgnoreCase("exit")) return;

            User receiver = users.stream()
                    .filter(u -> u.getDisplayName().equals(choice))
                    .findFirst().orElse(null);

            if (receiver != null) {
                String content = JOptionPane.showInputDialog("Enter your message:");
                if (content == null || content.equalsIgnoreCase("exit")) return;

                Message msg = new Message(sender.getDisplayName(), receiver.getDisplayName(), content);
                chatHistory.add(msg);
                saveMessages();
                JOptionPane.showMessageDialog(null, "📤 Message sent to " + receiver.getDisplayName() + "!");
            }
        }
    }

    public static void readMessages(User receiver) {
        boolean hasMessages = false;
        for (Message msg : chatHistory) {
            if (msg.receiver.equals(receiver.getDisplayName())) {
                msg.displayMessage();
                msg.markAsRead();
                hasMessages = true;
            }
        }
        if (!hasMessages) {
            JOptionPane.showMessageDialog(null, "No new messages.");
        }
        saveMessages();
    }

    public static void checkSentMessages(User sender) {
        boolean hasSent = false;
        for (Message msg : chatHistory) {
            if (msg.sender.equals(sender.getDisplayName())) {
                msg.displayMessage();
                hasSent = true;
            }
        }
        if (!hasSent) {
            JOptionPane.showMessageDialog(null, "No sent messages yet.");
        }
    }

    public static void main(String[] args) {
        loadUsers();
        loadMessages();

        while (true) {
            String[] mainOptions = {"Register New User", "Login and Chat", "Exit"};
            String mainChoice = (String) JOptionPane.showInputDialog(null,
                    "💬 Welcome to Mini WhatsApp GUI Chat!\n\nChoose an option:",
                    "Main Menu",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    mainOptions,
                    null);

            if (mainChoice == null || mainChoice.equalsIgnoreCase("exit")) break;

            switch (mainChoice) {
                case "Register New User":
                    registerNewUser();
                    break;
                case "Login and Chat":
                    User currentUser = loginUser();
                    if (currentUser == null) break;

                    boolean stayLoggedIn = true;
                    while (stayLoggedIn) {
                        String[] chatOptions = {"Send a Message", "Read Messages", "Check Sent Messages", "Logout", "Exit"};
                        String option = (String) JOptionPane.showInputDialog(null,
                                "What would you like to do?",
                                "Chat Menu",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                chatOptions,
                                null);

                        if (option == null || option.equalsIgnoreCase("exit")) System.exit(0);

                        switch (option) {
                            case "Send a Message":
                                sendMessage(currentUser);
                                break;
                            case "Read Messages":
                                readMessages(currentUser);
                                break;
                            case "Check Sent Messages":
                                checkSentMessages(currentUser);
                                break;
                            case "Logout":
                                stayLoggedIn = false;
                                break;
                        }
                    }
                    break;
            }
        }

        JOptionPane.showMessageDialog(null, "👋 Goodbye! Thanks for using Mini WhatsApp.");
    }
}
