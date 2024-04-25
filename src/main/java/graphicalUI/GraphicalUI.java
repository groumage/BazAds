package graphicalUI;

import client.Client;
import client.ClientState;
import server.Domain;
import server.Sale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class GraphicalUI {

    private Client client;
    private PerspectiveView view;
    private ArrayList<String> highlightedName;
    private String selectMessageBoxDst = null;
    private Domain selectDomain = null;
    private String selectSale;
    private Sale memoryAnnonce;
    private JPanel panel1;
    private JButton signIn;
    private JTextField mail;
    private JTextField user;
    private JButton signUp;
    private JButton connect;
    private JButton disconnect;
    private JList<Domain> domainsList;
    private JTextArea contentArea;
    private JPasswordField password;
    private JButton refreshDomainList;
    private JButton createSale;
    private JButton openChat;
    private JButton signOut;
    private JButton updateSale;
    private JButton deleteSale;
    private JList<Sale> sales;
    private JComboBox<Domain> domainsComboBox;
    private JCheckBox updateViewCheckBox;
    private JTextField titleAnnonceField;
    private JLabel statusLabel;
    private JButton refreshAnnonceListButton;
    private JLabel ownerLabel;
    private JTextField priceField;
    private JList<String> messageBoxList;
    private JButton sendButton;
    private JTextField chatMessage;
    private JTextArea contentMessage;
    private JCheckBox createViewCheckBox;

    public GraphicalUI(Client client) {
        this.client = client;
        $$$setupUI$$$();
        this.setStatusGUI(ClientState.DISCONNECTED);
        this.updateViewGUI(PerspectiveView.DISCONNECTED);
        this.highlightedName = new ArrayList<>();

        DefaultListModel<String> listModel = new DefaultListModel<>();
        this.messageBoxList.setModel(listModel);
        ((DefaultCaret) (this.contentMessage.getCaret())).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.messageBoxList.setCellRenderer(new MessageBoxListRenderer());

        this.sales.setCellRenderer(new AnnonceListRenderer());
        signUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.signUp(mail.getText(), user.getText(), String.valueOf(password.getPassword()));
                    mail.setText("");
                    user.setText("");
                    password.setText("");
                } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                         IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                         InvalidKeyException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        signIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.signIn(mail.getText(), String.valueOf(password.getPassword()), true);
                    mail.setText("");
                    user.setText("");
                    password.setText("");
                } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                         IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                         InvalidKeyException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        signOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.signOut();
                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                         IOException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException ex) {
                    throw new RuntimeException(ex);
                }
                client.getGui().updateViewGUI(PerspectiveView.CONNECTED);
            }
        });
        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.openSocketToCentralServer();
                    client.requestPublicKeyOfCentralServer();
                } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                         IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                         InvalidKeyException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.closeSocketToCentralServer();
                } catch (IOException | NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }
                updateViewGUI(PerspectiveView.DISCONNECTED);
            }
        });
        createViewCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (createViewCheckBox.isSelected())
                    updateViewGUI(PerspectiveView.CREATE_ANNONCE);
                else
                    updateViewGUI(PerspectiveView.LOGGED);
            }
        });
        messageBoxList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                contentMessage.setText(client.getMessage(messageBoxList.getSelectedValue()));
                highlightedName.remove(messageBoxList.getSelectedValue());
                messageBoxList.repaint();
                if (messageBoxList.getSelectedIndex() != -1)
                    selectMessageBoxDst = messageBoxList.getSelectedValue();
            }
        });
        createSale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.createSale((Domain) domainsComboBox.getSelectedItem(), titleAnnonceField.getText(), contentArea.getText(), Integer.parseInt(priceField.getText()));
                    titleAnnonceField.setText("");
                    contentArea.setText("");
                    priceField.setText("");
                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                         IOException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        updateViewCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updateViewCheckBox.isSelected()) {
                    memoryAnnonce = sales.getSelectedValue();
                    updateViewGUI(PerspectiveView.UPDATE_ANNONCE);
                } else {
                    memoryAnnonce = null;
                    //int memSelectDomain = domainList.getSelectedIndex();
                    updateViewGUI(PerspectiveView.LOGGED);
                    //if (memSelectDomain != -1)
                    //    domainList.setSelectedIndex(memSelectDomain);
                }
            }
        });
        domainsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !createViewCheckBox.isSelected() && !updateViewCheckBox.isSelected()) {
                    try {
                        sales.clearSelection();
                        domainsComboBox.removeAllItems();
                        client.salesFromDomain(domainsList.getSelectedValue());
                        selectDomain = domainsList.getSelectedValue();
                    } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                             IOException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        sales.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!sales.isSelectionEmpty()) {
                    titleAnnonceField.setText(sales.getSelectedValue().getTitle());
                    domainsComboBox.removeAllItems();
                    domainsComboBox.addItem(sales.getSelectedValue().getDomain());
                    domainsComboBox.setSelectedItem(sales.getSelectedValue().getDomain());
                    contentArea.setText(sales.getSelectedValue().getContent());
                    priceField.setText(String.valueOf(sales.getSelectedValue().getPrice()));
                    ownerLabel.setText(sales.getSelectedValue().getOwner());
                    updateViewCheckBox.setEnabled(ownerLabel.getText().equals(client.getName()));
                    selectSale = sales.getName();
                    deleteSale.setEnabled(sales.getSelectedValue().getOwner().equals(client.getName()));
                } else {
                    titleAnnonceField.setText("");
                    domainsComboBox.removeAllItems();
                    contentArea.setText("");
                    priceField.setText("");
                    ownerLabel.setText("N/A");
                }
            }
        });
        updateSale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.updateSale(titleAnnonceField.getText(), contentArea.getText(), Integer.parseInt(priceField.getText()), memoryAnnonce.getId());
                    titleAnnonceField.setText("");
                    contentArea.setText("");
                    priceField.setText("");
                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                         IOException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        deleteSale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.deleteSale(sales.getSelectedValue().getId());
                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                         IOException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public static void main(String[] args) {

    }

    public void updateViewGUI(PerspectiveView view) {
        this.view = view;
        switch (view) {
            case DISCONNECTED -> {
                this.mail.setText("");
                this.mail.setEnabled(false);
                this.mail.setEditable(false);
                this.password.setText("");
                this.password.setEnabled(false);
                this.password.setEditable(false);
                this.user.setText("");
                this.user.setEnabled(false);
                this.user.setEditable(false);
                this.titleAnnonceField.setText("");
                this.titleAnnonceField.setEditable(false);
                this.titleAnnonceField.setEnabled(false);
                this.priceField.setText("");
                this.priceField.setEditable(false);
                this.priceField.setEnabled(false);
                this.chatMessage.setText("");
                this.chatMessage.setEditable(false);
                this.chatMessage.setEnabled(false);

                this.domainsList.setModel(new DefaultListModel<>());
                this.domainsList.setEnabled(false);
                this.sales.setModel(new DefaultListModel<>());
                this.sales.setEnabled(false);
                this.messageBoxList.setModel(new DefaultListModel<>());
                this.messageBoxList.setEnabled(false);

                this.domainsComboBox.removeAllItems();
                this.domainsComboBox.setEnabled(false);
                this.domainsComboBox.setEditable(false);

                this.contentArea.setText("");
                this.contentArea.setEditable(false);
                this.contentArea.setEnabled(false);
                this.contentArea.setText("");
                this.contentMessage.setEditable(false);
                this.contentMessage.setEnabled(false);

                this.ownerLabel.setText("N/A");
                this.ownerLabel.setEnabled(false);

                this.refreshDomainList.setEnabled(false);
                this.refreshAnnonceListButton.setEnabled(false);
                this.signIn.setEnabled(false);
                this.signUp.setEnabled(false);
                this.signOut.setEnabled(false);
                this.connect.setEnabled(true);
                this.disconnect.setEnabled(false);
                this.createSale.setEnabled(false);
                this.updateSale.setEnabled(false);
                this.updateViewCheckBox.setEnabled(false);
                this.deleteSale.setEnabled(false);
                this.sendButton.setEnabled(false);
                this.openChat.setEnabled(false);

                this.createViewCheckBox.setEnabled(false);
                this.updateViewCheckBox.setEnabled(false);
            }
            case CONNECTED -> {
                this.mail.setText("");
                this.mail.setEnabled(true);
                this.mail.setEditable(true);
                this.password.setText("");
                this.password.setEnabled(true);
                this.password.setEditable(true);
                this.user.setText("");
                this.user.setEnabled(true);
                this.user.setEditable(true);
                this.titleAnnonceField.setText("");
                this.titleAnnonceField.setEditable(false);
                this.titleAnnonceField.setEnabled(false);
                this.priceField.setText("");
                this.priceField.setEditable(false);
                this.priceField.setEnabled(false);
                this.chatMessage.setText("");
                this.chatMessage.setEditable(false);
                this.chatMessage.setEnabled(false);

                this.domainsList.setModel(new DefaultListModel<>());
                this.domainsList.setEnabled(false);
                this.sales.setModel(new DefaultListModel<>());
                this.sales.setEnabled(false);
                this.updateMessageBoxList();
                this.messageBoxList.setEnabled(true);

                this.domainsComboBox.removeAllItems();
                this.domainsComboBox.setEnabled(false);
                this.domainsComboBox.setEditable(false);

                this.contentArea.setText("");
                this.contentArea.setEditable(false);
                this.contentArea.setEnabled(false);
                this.contentMessage.setEditable(false);
                this.contentMessage.setEnabled(true);

                this.ownerLabel.setText("N/A");
                this.ownerLabel.setEnabled(false);

                this.refreshDomainList.setEnabled(false);
                this.refreshAnnonceListButton.setEnabled(false);
                this.signIn.setEnabled(true);
                this.signUp.setEnabled(true);
                this.signOut.setEnabled(false);
                this.connect.setEnabled(false);
                this.disconnect.setEnabled(true);
                this.createSale.setEnabled(false);
                this.updateSale.setEnabled(false);
                this.updateViewCheckBox.setEnabled(false);
                this.deleteSale.setEnabled(false);
                this.sendButton.setEnabled(false);
                this.openChat.setEnabled(false);

                this.createViewCheckBox.setEnabled(false);
                this.updateViewCheckBox.setEnabled(false);
            }
            case LOGGED -> {
                this.mail.setText(this.client.getMail());
                this.mail.setEditable(false);
                this.mail.setEnabled(true);
                this.password.setText("");
                this.password.setEditable(false);
                this.password.setEnabled(false);
                this.user.setText(this.client.getName());
                this.user.setEditable(false);
                this.user.setEnabled(true);
                this.titleAnnonceField.setText("");
                this.titleAnnonceField.setEditable(false);
                this.titleAnnonceField.setEnabled(true);
                this.priceField.setText("");
                this.priceField.setEditable(false);
                this.priceField.setEnabled(true);
                this.chatMessage.setText("");
                this.chatMessage.setEditable(false);
                this.chatMessage.setEnabled(true);

                this.updateDomainList();
                this.domainsList.setEnabled(true);
                this.updateAnnonceList();
                this.sales.setEnabled(true);
                if (this.selectDomain != null)
                    this.clickOnDomain(selectDomain);
                if (selectSale != null)
                    this.clickOnAnnonce(this.selectSale);
                this.updateMessageBoxList();
                this.messageBoxList.setEnabled(true);

                this.domainsComboBox.removeAllItems();
                this.domainsComboBox.setEnabled(true);
                this.domainsComboBox.setEditable(false);

                this.contentArea.setText("");
                this.contentArea.setEditable(false);
                this.contentArea.setEnabled(true);
                this.contentMessage.setEditable(false);
                this.contentMessage.setEnabled(true);
                if (this.selectMessageBoxDst != null)
                    this.contentMessage.setText(this.client.getMessage(this.selectMessageBoxDst));

                this.ownerLabel.setText("N/A");
                this.ownerLabel.setEnabled(false);

                this.refreshDomainList.setEnabled(true);
                this.refreshAnnonceListButton.setEnabled(true);
                this.signIn.setEnabled(false);
                this.signUp.setEnabled(false);
                this.signOut.setEnabled(true);
                this.createSale.setEnabled(false);
                this.updateSale.setEnabled(false);
                this.deleteSale.setEnabled(false);
                this.connect.setEnabled(false);
                this.disconnect.setEnabled(false);
                this.openChat.setEnabled(false);

                this.updateViewCheckBox.setEnabled(false);
                this.createViewCheckBox.setEnabled(true);
            }
            case CREATE_ANNONCE -> {
                this.mail.setText(this.client.getMail());
                this.mail.setEditable(false);
                this.mail.setEnabled(true);
                this.password.setText("");
                this.password.setEditable(false);
                this.password.setEnabled(false);
                this.user.setText(this.client.getName());
                this.user.setEditable(false);
                this.user.setEnabled(true);
                this.titleAnnonceField.setText("");
                this.titleAnnonceField.setEditable(true);
                this.titleAnnonceField.setEnabled(true);
                this.priceField.setText("");
                this.priceField.setEditable(true);
                this.priceField.setEnabled(true);
                this.chatMessage.setText("");
                this.chatMessage.setEditable(false);
                this.chatMessage.setEnabled(false);

                this.domainsList.clearSelection();
                this.domainsList.setEnabled(false);
                //this.annonceList.setModel(new DefaultListModel<>());
                this.sales.clearSelection();
                this.sales.setEnabled(false);
                this.messageBoxList.setEnabled(true);

                this.updateDomainComboBox();
                this.domainsComboBox.setEnabled(true);
                this.domainsComboBox.setEditable(false);

                this.contentArea.setText("");
                this.contentArea.setEditable(true);
                this.contentArea.setEnabled(true);
                this.contentMessage.setEditable(false);
                this.contentMessage.setEnabled(true);

                this.ownerLabel.setText("Yourself");
                this.ownerLabel.setEnabled(false);

                this.refreshDomainList.setEnabled(false);
                this.refreshAnnonceListButton.setEnabled(false);
                this.signIn.setEnabled(false);
                this.signUp.setEnabled(false);
                this.signOut.setEnabled(false);
                this.createSale.setEnabled(true);
                this.updateSale.setEnabled(false);
                this.deleteSale.setEnabled(false);
                this.connect.setEnabled(false);
                this.disconnect.setEnabled(false);
                this.openChat.setEnabled(false);

                this.updateViewCheckBox.setEnabled(false);
                this.createViewCheckBox.setEnabled(true);
            }
            case UPDATE_ANNONCE -> {
                this.mail.setText(this.client.getMail());
                this.mail.setEditable(false);
                this.mail.setEnabled(true);
                this.password.setText("");
                this.password.setEditable(false);
                this.password.setEnabled(false);
                this.user.setText(this.client.getName());
                this.user.setEditable(false);
                this.user.setEnabled(true);
                this.titleAnnonceField.setText(sales.getSelectedValue().getTitle());
                this.titleAnnonceField.setEditable(true);
                this.titleAnnonceField.setEnabled(true);
                this.priceField.setText(String.valueOf(sales.getSelectedValue().getPrice()));
                this.priceField.setEditable(true);
                this.priceField.setEnabled(true);
                this.chatMessage.setText("");
                this.chatMessage.setEditable(false);
                this.chatMessage.setEnabled(false);

                this.domainsList.clearSelection();
                this.domainsList.setEnabled(false);
                //this.annonceList.setModel(new DefaultListModel<>());
                this.sales.clearSelection();
                this.sales.setEnabled(false);
                this.messageBoxList.setEnabled(true);

                this.updateDomainComboBox();
                this.domainsComboBox.setEnabled(true);
                this.domainsComboBox.setEditable(false);

                this.contentArea.setEditable(true);
                this.contentArea.setEnabled(true);
                this.contentMessage.setEditable(false);
                this.contentMessage.setEnabled(true);

                this.ownerLabel.setText("Yourself");
                this.ownerLabel.setEnabled(false);

                this.refreshDomainList.setEnabled(false);
                this.refreshAnnonceListButton.setEnabled(false);
                this.signIn.setEnabled(false);
                this.signUp.setEnabled(false);
                this.signOut.setEnabled(false);
                this.createSale.setEnabled(false);
                this.updateSale.setEnabled(true);
                this.deleteSale.setEnabled(false);
                this.connect.setEnabled(false);
                this.disconnect.setEnabled(false);
                this.openChat.setEnabled(false);

                this.updateViewCheckBox.setEnabled(true);
                this.createViewCheckBox.setEnabled(false);
            }
        }
    }

    public void clickSignUp() {
        this.signUp.doClick();
    }

    public void setMail(String mail) {
        this.mail.setText(mail);
    }

    public void setUsername(String username) {
        this.user.setText(username);
    }

    public void setPwd(String pwd) {
        this.password.setText(pwd);
    }

    public JPanel getPane() {
        return this.panel1;
    }

    public void clickSignIn() {
        this.signIn.doClick();
    }

    public void clickSignOut() {
        this.signOut.doClick();
    }

    public void clickCreateAnnonce() {
        this.createSale.doClick();
    }

    public void setStatusGUI(ClientState state) {
        this.statusLabel.setText(String.valueOf(state));
    }

    public PerspectiveView getPerspective() {
        return this.view;
    }

    public void clickCreateCheckBox() {
        this.createViewCheckBox.doClick();
    }

    public void clickUpdateCheckBox() {
        this.updateViewCheckBox.doClick();
    }

    public ClientState getStatusLabel() {
        return ClientState.valueOf(this.statusLabel.getText());
    }

    public void clickConnect() {
        this.connect.doClick();
    }

    public void clickRemoveAnnonce() {
        this.deleteSale.doClick();
    }

    public void clickUpdateAnnonce() {
        this.updateSale.doClick();
    }

    public void clickDisconnect() {
        this.disconnect.doClick();
    }

    public String[] getMessageBoxList() {
        ArrayList<String> listContent = new ArrayList<>();
        for (int i = 0; i < this.messageBoxList.getModel().getSize(); i++)
            listContent.add(this.messageBoxList.getModel().getElementAt(i));
        return listContent.toArray(new String[0]);
    }

    public JList<String> getMessageBox() {
        return this.messageBoxList;
    }

    public Sale[] getSales() {
        ArrayList<Sale> annonceContent = new ArrayList<>();
        for (int i = 0; i < this.sales.getModel().getSize(); i++)
            annonceContent.add(this.sales.getModel().getElementAt(i));
        return annonceContent.toArray(new Sale[0]);
    }

    public JList<Sale> getAnnonceJList() {
        return this.sales;
    }

    public Domain[] getDomainsList() {
        ArrayList<Domain> listContent = new ArrayList<>();
        for (int i = 0; i < this.domainsList.getModel().getSize(); i++)
            listContent.add(this.domainsList.getModel().getElementAt(i));
        return listContent.toArray(new Domain[0]);
    }

    public JList<Domain> getDomainJList() {
        return this.domainsList;
    }

    public Domain[] getDomainInComboBox() {
        ArrayList<Domain> listContent = new ArrayList<>();
        for (int i = 0; i < this.domainsComboBox.getModel().getSize(); i++)
            listContent.add(this.domainsComboBox.getModel().getElementAt(i));
        return listContent.toArray(new Domain[0]);
    }

    public JComboBox<Domain> getDomainsComboBox() {
        return this.domainsComboBox;
    }

    public void setDomainList(Domain[] domains) {
        this.client.setDomainsList(domains);
        this.updateDomainList();
    }

    public void updateDomainList() {
        DefaultListModel<Domain> demoList = new DefaultListModel<>();
        if (this.client.getDomains() != null) {
            for (Domain d : this.client.getDomains())
                demoList.addElement(d);
            this.domainsList.setModel(demoList);
        }
    }

    public void updateDomainComboBox() {
        this.domainsComboBox.removeAllItems();
        if (this.client.getDomains() != null) {
            for (Domain d : this.client.getDomains())
                this.domainsComboBox.addItem(d);
        }
    }

    public void updateAnnonceList() {
        DefaultListModel<Sale> demoList = new DefaultListModel<>();
        if (this.client.getSales() != null) {
            for (Sale a : this.client.getSales()) {
                demoList.addElement(a);
            }
        }
        this.sales.setModel(demoList);

    }

    public void updateMessageBoxList() {
        DefaultListModel<String> demoList = new DefaultListModel<>();
        if (this.client.getMessages() != null) {
            for (String k : this.client.getMessages().keySet())
                demoList.addElement(k);
            this.messageBoxList.setModel(demoList);
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(13, 6, new Insets(0, 0, 0, 0), -1, -1));
        mail = new JTextField();
        mail.setEditable(false);
        mail.setEnabled(true);
        mail.setText("");
        panel1.add(mail, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Mail address");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Password");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        user = new JTextField();
        panel1.add(user, new com.intellij.uiDesigner.core.GridConstraints(0, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Username");
        panel1.add(label3, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        password = new JPasswordField();
        panel1.add(password, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        signOut = new JButton();
        signOut.setText("Sign out");
        panel1.add(signOut, new com.intellij.uiDesigner.core.GridConstraints(3, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sales = new JList();
        sales.setEnabled(false);
        panel1.add(sales, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 6, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Annonce list");
        panel1.add(label4, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 5, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connect = new JButton();
        connect.setText("Connect");
        panel1.add(connect, new com.intellij.uiDesigner.core.GridConstraints(4, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        disconnect = new JButton();
        disconnect.setText("Disconnect");
        panel1.add(disconnect, new com.intellij.uiDesigner.core.GridConstraints(5, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateViewCheckBox = new JCheckBox();
        updateViewCheckBox.setText("Update view");
        panel1.add(updateViewCheckBox, new com.intellij.uiDesigner.core.GridConstraints(6, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        refreshDomainList = new JButton();
        refreshDomainList.setText("Refresh list");
        panel1.add(refreshDomainList, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        domainsList = new JList();
        panel1.add(domainsList, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Domain list");
        panel1.add(label5, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Status");
        panel1.add(label6, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setText("Label");
        panel1.add(statusLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Message box");
        panel1.add(label7, new com.intellij.uiDesigner.core.GridConstraints(11, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        messageBoxList = new JList();
        panel1.add(messageBoxList, new com.intellij.uiDesigner.core.GridConstraints(11, 1, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 3, new Insets(5, 5, 5, 5), -1, -1));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 10, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label8 = new JLabel();
        label8.setText("Title");
        panel2.add(label8, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleAnnonceField = new JTextField();
        panel2.add(titleAnnonceField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        domainsComboBox = new JComboBox();
        panel2.add(domainsComboBox, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Domain");
        panel2.add(label9, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        contentArea = new JTextArea();
        panel2.add(contentArea, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Content");
        panel2.add(label10, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        priceField = new JTextField();
        panel2.add(priceField, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Price");
        panel2.add(label11, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Owner");
        panel2.add(label12, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ownerLabel = new JLabel();
        ownerLabel.setText("Owner name");
        ownerLabel.setToolTipText("ownerLabel.getText()");
        panel2.add(ownerLabel, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openChat = new JButton();
        openChat.setText("Open chat");
        panel2.add(openChat, new com.intellij.uiDesigner.core.GridConstraints(4, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createSale = new JButton();
        createSale.setText("Create annonce");
        panel1.add(createSale, new com.intellij.uiDesigner.core.GridConstraints(8, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateSale = new JButton();
        updateSale.setText("Update annonce");
        panel1.add(updateSale, new com.intellij.uiDesigner.core.GridConstraints(9, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteSale = new JButton();
        deleteSale.setText("Remove annonce");
        panel1.add(deleteSale, new com.intellij.uiDesigner.core.GridConstraints(10, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        refreshAnnonceListButton = new JButton();
        refreshAnnonceListButton.setText("Refresh list");
        panel1.add(refreshAnnonceListButton, new com.intellij.uiDesigner.core.GridConstraints(10, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createViewCheckBox = new JCheckBox();
        createViewCheckBox.setText("Create view");
        panel1.add(createViewCheckBox, new com.intellij.uiDesigner.core.GridConstraints(7, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Send");
        panel1.add(sendButton, new com.intellij.uiDesigner.core.GridConstraints(12, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chatMessage = new JTextField();
        panel1.add(chatMessage, new com.intellij.uiDesigner.core.GridConstraints(12, 2, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        signUp = new JButton();
        signUp.setText("Sign up");
        panel1.add(signUp, new com.intellij.uiDesigner.core.GridConstraints(1, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        signIn = new JButton();
        signIn.setText("Sign in");
        panel1.add(signIn, new com.intellij.uiDesigner.core.GridConstraints(2, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(11, 2, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 50), null, null, 0, false));
        contentMessage = new JTextArea();
        contentMessage.setColumns(50);
        contentMessage.setLineWrap(true);
        contentMessage.setRows(8);
        contentMessage.setText("");
        contentMessage.setWrapStyleWord(true);
        contentMessage.putClientProperty("html.disable", Boolean.FALSE);
        scrollPane1.setViewportView(contentMessage);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

    private static class AnnonceListRenderer extends JLabel implements ListCellRenderer<Sale> {
        public AnnonceListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Sale> list, Sale value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.getTitle());
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

    private class MessageBoxListRenderer extends JLabel implements ListCellRenderer<String> {

        public MessageBoxListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            if (highlightedName.contains(value))
                setText(value + "(new)");
            return this;
        }
    }

    public void clickOnDomain(Domain domain) {
        for (int i = 0; i < this.domainsList.getModel().getSize(); i++)
            if (this.domainsList.getModel().getElementAt(i) == domain) {
                this.domainsList.setSelectedIndex(i);
                return;
            }
    }

    public void clickOnAnnonce(String title) {
        for (int i = 0; i < this.sales.getModel().getSize(); i++) {
            if (this.sales.getModel().getElementAt(i).getTitle().equals(title)) {
                this.sales.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Highlight the message box with the given name.
     * 
     * @param dst
     */
    public void highlightDst(String dst) {
        if (!this.highlightedName.contains(dst) && (this.selectMessageBoxDst == null || !this.selectMessageBoxDst.equals(dst)))
            this.highlightedName.add(dst);
        this.messageBoxList.repaint();
        if (selectMessageBoxDst != null)
            if (selectMessageBoxDst.equals(dst))
                this.contentMessage.setText(this.client.getMessage(dst));
    }

    public JTextField getMail() {
        return this.mail;
    }

    public JPasswordField getPassword() {
        return this.password;
    }

    public JTextField getUser() {
        return this.user;
    }

    public JTextField getTitleAnnonceField() {
        return this.titleAnnonceField;
    }

    public JTextField getPriceField() {
        return this.priceField;
    }

    public JTextArea getContentAnnonce() {
        return this.contentArea;
    }

    public JCheckBox getUpdateCheckBox() {
        return this.updateViewCheckBox;
    }

    public JButton getDeleteSale() {
        return deleteSale;
    }
}
