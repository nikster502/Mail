/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author AmaiYuki
 */
public class PostForm extends javax.swing.JFrame {

    public AuthForm authForm;
    public MailAuthenticator mailAuthenticator;
    public Folder[] folder;
    public String Memlog = "";
    public String email = mailAuthenticator.email;
    public String password = mailAuthenticator.password;
    public String email1 = authForm.email;
    public String password1 = authForm.password;
    String FILENAME = "file.spb";
    public Store store;
    public Mail mail;
    public static String IMAP_AUTH_EMAIL;
    public static String IMAP_AUTH_PWD;
    public String IMAP_Server,IMAP_Port;

    /**
     * Creates new form PostForm
     */
    public void Readfile() {
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);
            String sCurrentLine;
            StringBuffer sb = new StringBuffer();
            br = new BufferedReader(new FileReader(FILENAME));
            while ((sCurrentLine = br.readLine()) != null) {
                //System.out.println(sCurrentLine);
                sb.append(sCurrentLine);
            }
            String[] lines = sb.toString().split(":");

            Memlog = lines[0];
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Session Init() {
        if (email1 != null && password1 != null) {
            IMAP_AUTH_EMAIL = email1;
            IMAP_AUTH_PWD = password1;
        } else {
            IMAP_AUTH_EMAIL = email;
            IMAP_AUTH_PWD = password;
        }

        IMAP_Server = " ";
        IMAP_Port = "993";

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
        return session;
    }

    public void ConnectMail() {
        Session session = Init();
        session.setDebug(false);
        try {
            store = session.getStore();
            // Подключение к почтовому серверу
            store.connect(IMAP_Server, IMAP_AUTH_EMAIL, IMAP_AUTH_PWD);
            folder = store.getDefaultFolder().list();
            final String[] TH = {"Адрес отправителя", "Тема", "Дата"};
            final DefaultTableModel model = new DefaultTableModel(TH, 0);
            jTable1.setModel(model);
            String[][] Data;
//        jTree = new javax.swing.JTree();
//        final JTree jTree = new JTree(new Object[]{getFolder()});
            javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode(email);
            for (Folder fd : getFolder()) {
                javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode(fd.getName());
                treeNode1.add(treeNode2);
            }

            jTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
            jScrollPane1.setViewportView(jTree);

            jTree.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                   Session session = Init();
        session.setDebug(false);
        try {
            store = session.getStore();
            // Подключение к почтовому серверу
            store.connect(IMAP_Server, IMAP_AUTH_EMAIL, IMAP_AUTH_PWD);
            folder = store.getDefaultFolder().list();
            
                    listMessages.clear();
                    ArrayList<String> attachments = new ArrayList<String>();

                    TreePath tp = jTree.getPathForLocation(me.getX(), me.getY());
                    if (tp != null) {
                        System.out.println(tp.getPathComponent(1).toString());
                        String S = tp.getPathComponent(1).toString();
                        try {
                            Folder emailFolder = store.getFolder(S);
                            emailFolder.open(Folder.READ_ONLY);
                            Message[] messages = emailFolder.getMessages();
                            listMessages = getPart(messages, attachments);
                            for (int i = 0; i < listMessages.size(); i++) {
                                Object[] ob =null;
                                if (listMessages.get(i)!=null)
                                        ob=new Object[]{listMessages.get(i).getFrom(), listMessages.get(i).getSubject(), listMessages.get(i).getDateSent()};
                                if (ob!=null)
                                {
                                model.addRow(ob);
                                System.out.println("" + listMessages.get(i).getFrom());
                                }
                            }
                        } catch (MessagingException ex) {
                            Logger.getLogger(PostForm.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(PostForm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    } catch (MessagingException e) {
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Неверный логин или пароль");
        }
                }

            });

            jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent event) {
//                if (!event.getValueIsAdjusting()) {
//                    if (!isDelete) {
//                        if (file.exists()) {
//                            try {
//                                FileInputStream fi = new FileInputStream(file);
//                                ObjectInputStream is = new ObjectInputStream(fi);
//                                if (fi.available() > 0) {
//                                    messageList = (LinkedList<MessageBean>) is.readObject();
//                                }
//                                is.close();
//                                fi.close();
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            } catch (ClassNotFoundException e) {
//                                e.printStackTrace();
//                            }
//                        }

                    int selectedRow = jTable1.getSelectedRow();
                    MessageBean message = listMessages.get(selectedRow);
                    listMessages.get(selectedRow).setNew(false);

//                            file.delete();
//                            FileOutputStream fo = new FileOutputStream(file);
//                            ObjectOutputStream os = new ObjectOutputStream(fo);
//                            os.writeObject(messageList);
//                            os.close();
//                            fo.close();
                    jTextArea1.setText(message.getContent());
                    jTextArea1.setCaretPosition(0);
//                            mainForm.getAttachPanel().deleteAttachButton();
//                            if (message.getAttachments()!=null){
//                                for (int i=0; i<message.getAttachments().size();i++){
//                                    String path = System.getProperty("user.dir")+"\\"+message.getAttachments().get(i);
//                                    mainForm.getAttachPanel().addAttachButton(path);
//                                    mainForm.getAttachPanel().updateUI();
//                                }
//                            }
//                                if (mainForm.getTreePanel().getCount()>0) {
//                                    mainForm.getTreePanel().create(mainForm.getAddress(), mainForm.getListPanel(), mainForm.getTreePanel().getCount()-1);
//                                    mainForm.getTreePanel().getTree().expandPath(mainForm.getTreePanel().getTree().getPathForRow(1));
//                                }
//                            setUnBoldRow(selectedRow);

//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    } else isDelete = false;
//
                }
            }
            );
        } catch (MessagingException e) {
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Неверный логин или пароль");
        }
    }

    public Folder[] getFolder() {
        return folder;
    }

    public PostForm() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTree = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("JTree");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("почта");
        treeNode1.add(treeNode2);
        jTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(jTree);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(jTable1);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jMenu4.setText("Ящик");

        jMenuItem1.setText("Добавить новый");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem1);

        jMenuItem2.setText("Удалить");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem2);

        jMenuBar1.add(jMenu4);

        jMenu1.setText("Сообщения");

        jMenuItem3.setText("Написать письмо");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu7.setText("Справка");
        jMenuBar1.add(jMenu7);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        //УДАЛЕНИЕ АККАУНТА ПОЧТЫ!!#################################
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        this.dispose();
        authForm.setVisible(true);

    }//GEN-LAST:event_jMenuItem1ActionPerformed

    LinkedList<MessageBean> listMessages = new LinkedList<MessageBean>();

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

    private String saveFile(String filename, InputStream input) throws IOException {
        String path=new File(".").getCanonicalPath()+ "\\attachments\\" + filename;
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

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        ConnectMail();
        this.revalidate();
    }//GEN-LAST:event_formWindowOpened

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        new SendMail().setVisible(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PostForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PostForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PostForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PostForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PostForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTree jTree;
    // End of variables declaration//GEN-END:variables
}

//jTree.addMouseListener(new MouseAdapter() {
//      public void mouseClicked(MouseEvent me) {
//        TreePath tp = jTree.getPathForLocation(me.getX(), me.getY());
//    if (tp != null)
//    {
//     System.out.println(tp.getPathComponent(2).toString());
//     String S = tp.getPathComponent(2).toString();
//            try {
//     Folder emailFolder = store.getFolder(S);
//     emailFolder.open(Folder.READ_ONLY);
//                Message[] messages = emailFolder.getMessages();
//                System.out.println("messages.length---" + messages.length);
//al.clear();
//      for (int i = 0; i< messages.length; i++) {
//         Message message = messages[i];
//         System.out.println("---------------------------------");
//         System.out.println("Email Number " + (i + 1));
//         System.out.println("Subject: " + message.getSubject());
//         System.out.println("From: " + message.getFrom()[0]);
//          printMessage(message);
////          System.out.println(message.getContentType());
////         if (getText(message)!=null)
//
////         System.out.println("Text: " +getText(message));
////if (messages[i].getContent()!=null)
////{
////Multipart multipart = (Multipart) messages[i].getContent();
////
////    for (int j = 0; j < multipart.getCount(); j++) {
////
////        BodyPart bodyPart = multipart.getBodyPart(j);
////
////        String disposition = bodyPart.getDisposition();
////
////          if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) { // BodyPart.ATTACHMENT doesn't work for gmail
////              System.out.println("Mail have some attachment");
////
////              DataHandler handler = bodyPart.getDataHandler();
////              System.out.println("file name : " + handler.getName());                                 
////            }
////          else { 
////              System.out.println("Body: "+bodyPart.getContent());
//////              content= bodyPart.getContent().toString();
////            }
////    }
////}
//
//
//
//      }
//       emailFolder.close(false);
//       
//       for (int i = 0; i< al.size(); i++){
//            System.out.println("MAIL FROM : " + al.get(i).FROM);
//                       System.out.println("MAIL SUB : " + al.get(i).SUBJECT);
//            System.out.println("MAIL TEXT : " + al.get(i).CONTENT);
// 
//       }
//           
//            } catch (MessagingException ex) {
//                Logger.getLogger(PostForm.class.getName()).log(Level.SEVERE, null, ex);
//            }
////            catch (IOException ex) {
////             Logger.getLogger(PostForm.class.getName()).log(Level.SEVERE, null, ex);
////         }
//    }
//    else
//      System.out.println("");
//      }
//    });
// 
//        // TODO add your handling code here:
//    private boolean textIsHtml = false;
//
//    /**
//     * Return the primary text content of the message.
//     */
//    private String getText(Part p)  {
//        try
//        {
//        if (p.isMimeType("text/*")) {
//            String s = (String)p.getContent();
//            textIsHtml = p.isMimeType("text/html");
//            return s;
//        }
//
//        if (p.isMimeType("multipart/alternative")) {
//            // prefer html text over plain text
//            Multipart mp = (Multipart)p.getContent();
//            String text = null;
//            for (int i = 0; i < mp.getCount(); i++) {
//                Part bp = mp.getBodyPart(i);
//                if (bp.isMimeType("text/plain")) {
//                    if (text == null)
//                        text = getText(bp);
//                    continue;
//                } else if (bp.isMimeType("text/html")) {
//                    String s = getText(bp);
//                    if (s != null)
//                        return s;
//                } else {
//                    return getText(bp);
//                }
//            }
//            return text;
//        } else if (p.isMimeType("multipart/*")) {
//            Multipart mp = (Multipart)p.getContent();
//            for (int i = 0; i < mp.getCount(); i++) {
//                String s = getText(mp.getBodyPart(i));
//                if (s != null)
//                    return s;
//            }
//        }
//            } catch (MessagingException ex) {
//                Logger.getLogger(PostForm.class.getName()).log(Level.SEVERE, null, ex);
//                return null;
//            }
//            catch (IOException ex) {
//                
//             Logger.getLogger(PostForm.class.getName()).log(Level.SEVERE, null, ex);
//             return null;
//         }
//
//        return null;
//    }
//    ArrayList<ReadMail> al = new ArrayList<ReadMail>();
//public String printMessage(Message message) {
//
//    String myMail = "";
//    ReadMail rm=new ReadMail("", myMail);
//
//    try {
//        // Get the header information
//        String from = ((InternetAddress) message.getFrom()[0])
//                .getPersonal();
//
//
//
//        if (from == null)
//            from = ((InternetAddress) message.getFrom()[0]).getAddress();
//        System.out.println("FROM: " + from);
//        rm.setFROM(from);
//        String subject = message.getSubject();
//        System.out.println("SUBJECT: " + subject);
//        rm.setSUBJECT(subject);
//        // -- Get the message part (i.e. the message itself) --
//        Part messagePart = message;
//        Object content = messagePart.getContent();
//        // -- or its first body part if it is a multipart message --
//        if (content instanceof Multipart) {
//            messagePart = ((Multipart) content).getBodyPart(0);
//            System.out.println("[ Multipart Message ]");
//        }
//        // -- Get the content type --
//        String contentType = messagePart.getContentType();
//        // -- If the content is plain text, we can print it --
//        System.out.println("CONTENT:" + contentType);
//        if (contentType.startsWith("TEXT/PLAIN")
//                || contentType.startsWith("TEXT/HTML")) {
//            InputStream is = messagePart.getInputStream();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(is));
//            String thisLine = reader.readLine();
//            while (thisLine != null) {
//                System.out.println(thisLine);
//                myMail = myMail + thisLine;
//                thisLine = reader.readLine();
//            }
//
//
//        }
//        System.out.println("-----------------------------");
//    } catch (Exception ex) {
//        al.add(rm);
//        ex.printStackTrace();
//    }
//rm.setCONTENT(myMail);
//al.add(rm);
//    return myMail;
//
//}
