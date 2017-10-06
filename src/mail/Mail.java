/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;

/**
 *
 * @author AmaiYuki
 */
public class Mail {

    public AuthForm authForm;
    public PostForm postForm;
    public MailAuthenticator mailAuthenticator;
    final String ENCODING = "koi8-r";
    public Authenticator auth;
    public Store store;


//    public void send(String login, String password, String from, String to, String content, String subject, ArrayList<String> attachments, String smtpPort, String smtpHost) throws MessagingException, UnsupportedEncodingException {
//        Properties props = System.getProperties();
//        props.put("mail.smtp.port", smtpPort);
//        props.put("mail.smtp.host", smtpHost);
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.mime.charset", ENCODING);
//
//        auth = new MyAuthenticator(login, password);
//        Session session = Session.getDefaultInstance(props, auth);
//
//        MimeMessage msg = new MimeMessage(session);
//
//        msg.setFrom(new InternetAddress(from));
//        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
//        msg.setSubject(subject, ENCODING);
//
//        if (attachments.size() == 0)
//            msg.setText(content, ENCODING);
//        else {
//            BodyPart messageBodyPart = new MimeBodyPart();
//            messageBodyPart.setContent(content, "text/plain; charset=" + ENCODING + "");
//            Multipart multipart = new MimeMultipart();
//            multipart.addBodyPart(messageBodyPart);
//            addAttachments(attachments, multipart);
//            msg.setContent(multipart);
//        }
//
//        Transport.send(msg);
//    }
    protected void addAttachments(ArrayList<String> attachments, Multipart multipart) throws MessagingException, UnsupportedEncodingException {
        for (int i = 0; i < attachments.size(); i++) {
            String filename = attachments.get(i);
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filename);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(MimeUtility.encodeText(source.getName()));
            multipart.addBodyPart(attachmentBodyPart);
        }
    }

    public LinkedList<MessageBean> receive() throws MessagingException, IOException {
        
        System.out.println("ATATATA");
        String IMAP_AUTH_EMAIL = postForm.IMAP_AUTH_EMAIL;
        String IMAP_AUTH_PWD = postForm.IMAP_AUTH_PWD;

        System.out.println(IMAP_AUTH_EMAIL);
        String IMAP_Server = " ";
        String IMAP_Port = "993";

        if (IMAP_AUTH_EMAIL.contains("@gmail.com")) {
            IMAP_Server = "imap.gmail.com";
        } else {
            if (IMAP_AUTH_EMAIL.contains("@yandex.ru")) {
                IMAP_Server = "imap.yandex.ru";
            } else {
                if (IMAP_AUTH_EMAIL.contains("@rambler.ru")) {
                    IMAP_Server = "imap.rambler.ru";
                }
            }
        }

        Properties properties = new Properties();
        properties.put("mail.debug", "false");
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.port", IMAP_Port);

        Authenticator auth = new EmailAuth(IMAP_AUTH_EMAIL, IMAP_AUTH_PWD);
        Session session = Session.getDefaultInstance(properties, auth);
        session.setDebug(false);
        store = session.getStore();

        // Подключение к почтовому серверу
        store.connect(IMAP_Server, IMAP_AUTH_EMAIL, IMAP_AUTH_PWD);
        System.out.println("CONNECT!");
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        ArrayList<String> attachments = new ArrayList<String>();

        LinkedList<MessageBean> listMessages = getPart(messages, attachments);

        inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
        inbox.close(false);
        store.close();
        return listMessages;
    }

    private LinkedList<MessageBean> getPart(Message[] messages, ArrayList<String> attachments) throws MessagingException, IOException {
        LinkedList<MessageBean> listMessages = new LinkedList<MessageBean>();
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        for (Message inMessage : messages) {
            attachments.clear();
            if (inMessage.isMimeType("text/plain")) {
                MessageBean message = new MessageBean(inMessage.getMessageNumber(), MimeUtility.decodeText(inMessage.getSubject()), inMessage.getFrom()[0].toString(), null, f.format(inMessage.getSentDate()), (String) inMessage.getContent(), false, null);
                listMessages.add(message);
            } else if (inMessage.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) inMessage.getContent();
                MessageBean message = null;
                for (int i = 0; i < mp.getCount(); i++) {
                    Part part = mp.getBodyPart(i);
                    if ((part.getFileName() == null || part.getFileName() == "") && part.isMimeType("text/plain")) {
                        message = new MessageBean(inMessage.getMessageNumber(), inMessage.getSubject(), inMessage.getFrom()[0].toString(), null, f.format(inMessage.getSentDate()), (String) part.getContent(), false, null);
                    } else if (part.getFileName() != null || part.getFileName() != "") {
                        if ((part.getDisposition() != null) && (part.getDisposition().equals(Part.ATTACHMENT))) {
                            attachments.add(saveFile(MimeUtility.decodeText(part.getFileName()), part.getInputStream()));
                            if (message != null) {
                                message.setAttachments(attachments);
                            }
                        }
                    }
                }
                listMessages.add(message);
            }
        }
        return listMessages;
    }

    private String saveFile(String filename, InputStream input) {
        String path = "attachments\\" + filename;
        try {
            byte[] attachment = new byte[input.available()];
            input.read(attachment);
            File file = new File(path);
            FileOutputStream out = new FileOutputStream(file);
            out.write(attachment);
            input.close();
            out.close();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

//    public void delete(String user, String password, String host, int n) throws MessagingException, IOException {
//        Authenticator auth = new MyAuthenticator(user, password);
//
//        Properties props = System.getProperties();
//        props.put("mail.user", user);
//        props.put("mail.host", host);
//        props.put("mail.debug", "false");
//        props.put("mail.store.protocol", "pop3");
//        props.put("mail.transport.protocol", "smtp");
//
//        Session session = Session.getDefaultInstance(props, auth);
//        Store store = session.getStore();
//        store.connect();
//        Folder inbox = store.getFolder("INBOX");
//        inbox.open(Folder.READ_WRITE);
//
//        inbox.setFlags(n, n, new Flags(Flags.Flag.DELETED), true);
//        inbox.close(true);
//        store.close();
//    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MailAuthenticator().setVisible(true);
            }
        });

    }
}
