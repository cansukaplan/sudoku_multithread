package Sudoku;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Sudoku extends Thread {
    private final int kareSayisi = 9;
    private final int atlamaSayisi = 10;
    private boolean bittimiKontrollu = false;
    public boolean cozenThreadBumu = false;
    private static boolean cozuldu = false;
    long start, end;
    private int[][] matrisSudoku;
    private File dosya;

    public List<int[][]> cozumYollari;

    public Sudoku(String threadAdi) {
        setName(threadAdi);
        cozumYollari = new ArrayList<>();
    }

    public Sudoku(String threadAdi, int[][] matrisSudoku) {
        setName(threadAdi);
        cozumYollari = new ArrayList<>();
        this.matrisSudoku = matrisSudoku;
    }

    /**
     * thread burda basliyor
     */
    public void run() {
        int i, j, deger;
        Random generator = new Random();
        i = generator.nextInt(8);
        j = generator.nextInt(8);
        deger = generator.nextInt(8) + 1;


        start = System.currentTimeMillis();

        /**
         * matrisSudoku genel bir değer eğer onu kullanırsak tüm thread aynı değişimleri aynı arrayden yapar
         * ondan dolayı kopyasını kullanıyoruz
         */
        int[][] copyalanmisMatris = new int[kareSayisi][];
        for (int k = 0; k < kareSayisi; k++) {
            copyalanmisMatris[k] = Arrays.copyOf(matrisSudoku[k], matrisSudoku[k].length);
        }
        if (coz(i, j, copyalanmisMatris, 0, deger) && cozuldu)    // çözme burda başlıycak
        {
            //durumuYazdır(board);
            // System.out.println("SudokuThread çözümünü " + getName() + " tamamladı");
        } else {
            System.out.println("Bu sodoku c�z�lemedi");
        }


        if (!bittimiKontrollu) {
            bittimiKontrollu = true;
            //System.out.print("Tamamlanması: "+(end - start)+" ms sürdü" + "\n");
        }


    }

    public void threadleriCalistir(int[][] matrisSudoku) {
        Sudoku thread1 = new Sudoku("Thread 1", matrisSudoku);
        Sudoku thread2 = new Sudoku("thread 2", matrisSudoku);
        Sudoku thread3 = new Sudoku("thread 3", matrisSudoku);

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.interrupt();
        thread2.interrupt();
        thread3.interrupt();

        boolean yazdirildi = false;

        while (!yazdirildi) {
            if (!thread1.isAlive() && !thread2.isAlive() && !thread3.isAlive()) {
                System.out.println("thread 1 son asama: ");
                thread1.durumuYazdir(thread1.cozumYollari.get(1));

                System.out.println("thread 2 son asama: ");
                thread2.durumuYazdir(thread2.cozumYollari.get(1));

                System.out.println("thread 3 son asama: ");
                thread3.durumuYazdir(thread3.cozumYollari.get(1));
                yazdirildi = true;

                sonucuDosyayaYaz(thread1);
                sonucuDosyayaYaz(thread2);
                sonucuDosyayaYaz(thread3);

                ekrandaGoster(thread1, thread2, thread3);
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku("Run");
        sudoku.ilkEkraniAc();
    }

    public void ilkEkraniAc() {
        JFrame frame = new JFrame("SudokuThread");
        frame.setSize(300, 300);
        Button button = new Button("Dosya Ac");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                int cevap = jfc.showOpenDialog(null);
                if (cevap == JFileChooser.APPROVE_OPTION) {
                    dosya = jfc.getSelectedFile();
                    frame.dispose();
                    matrisSudoku = dosyayiSudokuyaCevir(dosya);
                    threadleriCalistir(matrisSudoku);
                }
            }
        });
        frame.add(button);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void ekrandaGoster(Sudoku thread1, Sudoku thread2, Sudoku thread3) {
        JFrame cozumEkrani = new JFrame();
        cozumEkrani.setTitle("C�z�m Ekrani");
        cozumEkrani.setSize(500, 500);

        Sudoku cozumYapanThread = null;
        if (thread1.cozenThreadBumu) {
            cozumYapanThread = thread1;
        } else if (thread2.cozenThreadBumu) {
            cozumYapanThread = thread2;
        } else if (thread3.cozenThreadBumu) {
            cozumYapanThread = thread3;
        }
        cozumEkrani.add(ekranCiz(cozumYapanThread, thread1, thread2, thread3, cozumEkrani));
        cozumEkrani.pack();
        cozumEkrani.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cozumEkrani.setVisible(true);
    }

    private JPanel ekranCiz(Sudoku sudoku, Sudoku thread1, Sudoku thread2, Sudoku thread3, JFrame cozumEkrani) {
        JPanel anaPanel = new JPanel();
        anaPanel.setLayout(new BorderLayout());
        anaPanel.add(new JLabel("Sudokuyu c�zen Thread: " + sudoku.getName()
                + " atlama s�resi: " + (sudoku.end - sudoku.start) + "ms"), BorderLayout.NORTH);
        //----------------------------------------------------------------------------------
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        panel.setLayout(new GridLayout(9, 9));

        for (int i = 0; i < kareSayisi; i++) {
            for (int j = 0; j < kareSayisi; j++) {
                JButton button = new JButton(sudoku.cozumYollari.get(sudoku.cozumYollari.size() - 1)[i][j] + "");

                if ((i < 3 && j < 3) || (i > 5 && j < 3) || (i < 3 && j > 5) || (i > 5 && j > 5) || (i > 2 && i < 6 && j > 2 && j < 6)) {
                    button.setBackground(Color.orange);
                }
                button.setEnabled(false);

                panel.add(button);
            }
        }

        anaPanel.add(panel);
        //----------------------------------------------------------------------------------

        JPanel pnlButtonlar = new JPanel();
        pnlButtonlar.setBackground(Color.white);

        pnlButtonlar.add(new JLabel("Threadlerin adimlarini g�rmek icin ilgili threade tiklayin"));
        JButton btnThred1 = new JButton("Thread 1");
        JButton btnThred2 = new JButton("Thread 2");
        JButton btnThred3 = new JButton("Thread 3");
        btnThred1.addActionListener(threadIzlemeEkraniOlustur(thread1, cozumEkrani));
        btnThred2.addActionListener(threadIzlemeEkraniOlustur(thread2, cozumEkrani));
        btnThred3.addActionListener(threadIzlemeEkraniOlustur(thread3, cozumEkrani));
        pnlButtonlar.add(btnThred1);
        pnlButtonlar.add(btnThred2);
        pnlButtonlar.add(btnThred3);
        anaPanel.add(pnlButtonlar, BorderLayout.SOUTH);
        //----------------------------------------------------------------------------------
        return anaPanel;
    }

    private ActionListener threadIzlemeEkraniOlustur(Sudoku izlenecekThread, Frame frame) {
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog(frame);
                dialog.setTitle(izlenecekThread.getName());

                JPanel panel = new JPanel(new GridLayout(izlenecekThread.cozumYollari.size() / 4, 4));
                panel.setBackground(Color.white);

                int j = 0;
                for (int i = 0; i < izlenecekThread.cozumYollari.size(); i = i + atlamaSayisi) {
                    panel.add(ekranCiz(izlenecekThread.cozumYollari.get(i), j));
                    j++;
                }

                JScrollPane scrollPane = new JScrollPane(panel);

                dialog.setContentPane(scrollPane);
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                dialog.setSize(1000, 700);
                dialog.setVisible(true);
            }
        };
        return listener;
    }

    private JPanel ekranCiz(int[][] cozumYolu, int asama) {
        JPanel anaPanel = new JPanel(new BorderLayout());
        anaPanel.setBackground(Color.WHITE);
        anaPanel.add(new Label(asama + 1 + ".Asama"), BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        panel.setLayout(new GridLayout(9, 9));
        panel.setBorder(new LineBorder(Color.BLUE, 4));
        for (int i = 0; i < kareSayisi; i++) {
            for (int j = 0; j < kareSayisi; j++) {
                JButton button = new JButton(cozumYolu[i][j] + "");
                if (cozumYolu[i][j] == 0) {
                    button.setText("*");
                }
                button.setEnabled(false);
                if ((i < 3 && j < 3) || (i > 5 && j < 3) || (i < 3 && j > 5) || (i > 5 && j > 5) || (i > 2 && i < 6 && j > 2 && j < 6)) {
                    button.setBackground(Color.orange);
                }
                panel.add(button);
            }
        }
        anaPanel.add(panel);
        return anaPanel;
    }

    /**
     * bura şimdilik elle dosyadan okuycak ilerde
     */
    public int[][] dosyayiSudokuyaCevir(File dosya) {
        int[][] newBoard = new int[kareSayisi][kareSayisi];

        int i = 0, j = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(dosya))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                for (int k = 0; k < satir.length(); k++) {
                    if (satir.charAt(k) != '*') {
                        newBoard[i][k] = Integer.parseInt(satir.charAt(k) + "");
                    } else {
                        newBoard[i][k] = 0;
                    }
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newBoard;
    }

    boolean coz(int satir, int sutun, int[][] tahta, int adim, int denemeYapilacakDeger) {
//        System.out.println("----------------------------------------------------");
//        System.out.println(currentThread().getName());
//        durumuYazdır(board);
//        System.out.println("----------------------------------------------------");
        try {
            Random r = new Random();
            Thread.sleep(r.nextInt(3));
        } catch (InterruptedException e) {
            //  e.printStackTrace();
        }

        if (cozuldu) {
            interrupt();
            return true;
        }
        if (adim == 81 && !cozuldu) {
            end = System.currentTimeMillis();
            System.out.println("cozuldu yapan: " + getName());
            System.out.print("Tamamlanmasi: " + (end - start) + " ms s�rd�" + "\n");
            cozuldu = true;
            cozenThreadBumu = true;
            return true;
        }
        if (bittimiKontrollu) {
            cozuldu = true;
            return true;
        }

        if (++sutun == kareSayisi) {
            sutun = 0;
            if (++satir == kareSayisi)
                satir = 0;
        }


        if (tahta[satir][sutun] != 0) {  // yaptıysak boşa bakma
            return coz(satir, sutun, tahta, adim + 1, denemeYapilacakDeger);
        }

        /*
         * En önemli yer bütün deneme burda yapılıyor
         */
        for (int d = 1; d <= kareSayisi; ++d) {
            if (++denemeYapilacakDeger == 10) denemeYapilacakDeger = 1;
            // altdaki if oraya bu deger konulabilirmi diye bakıyor
            if (dene(satir, sutun, denemeYapilacakDeger, tahta)) {
                tahta[satir][sutun] = denemeYapilacakDeger;   // çözdü yani bi kareye değer verdi

                int[][] tarihce = new int[kareSayisi][];
                for (int i = 0; i < kareSayisi; i++) {
                    tarihce[i] = Arrays.copyOf(tahta[i], tahta[i].length);
                }
                cozumYollari.add(tarihce);

                if (coz(satir, sutun, tahta, adim + 1, denemeYapilacakDeger))
                    return true;
            }
        }

        tahta[satir][sutun] = 0; //sıkıntı oldu 0 atıyor yani orası çözülemedi
        return false;
    }

    /**
     * bu deger buraya konulabilirmi
     */
    boolean dene(int row, int col, int value, int[][] board) {
        int i;
        for (i = 0; i < kareSayisi; i++) {
            // satirlara bak
            if (board[row][i] == value)
                return false;
            // sutunlara bak
            if (board[i][col] == value)
                return false;
            // karelere bak
            if (board[row / 3 * 3 + i % 3][col / 3 * 3 + i / 3] == value)
                return false;
        }
        return true; // sikinti
    }

    void durumuYazdir(int[][] matris) {

        int i, j;

        for (i = 0; i < kareSayisi; i++) {
            if (i % 3 == 0)
                System.out.println(" -----------------------");

            for (j = 0; j < kareSayisi; j++) {
                if (j % 3 == 0) System.out.print("| ");
                if (matris[i][j] == 0)
                    System.out.print("* ");
                else
                    System.out.print(Integer.toString(matris[i][j]) + " ");
            }
            System.out.println("|");
        }
        System.out.println(" -----------------------");
    }

    String durumu2String(int[][] matris) {
        String str = "";
        int i, j;

        for (i = 0; i < kareSayisi; i++) {
            if (i % 3 == 0)
                str += " -----------------------";

            for (j = 0; j < kareSayisi; j++) {
                if (j % 3 == 0) System.out.print("| ");
                if (matris[i][j] == 0)
                    str += "* ";
                else
                    str += Integer.toString(matris[i][j]) + " ";
            }
            str += "|";
        }
        str += " -----------------------";
        return str;
    }

    void sonucuDosyayaYaz(Sudoku sudoku) {
        Path path = Paths.get("./" + sudoku.getName() + ".txt");
        List<int[][]> cozumYollari = sudoku.cozumYollari;
        int adimSayisi = 1;
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (int s = 0; s < cozumYollari.size(); s = s + atlamaSayisi) {
                int[][] matris = cozumYollari.get(s);
                writer.write("--------------------------" + adimSayisi + ". Ad�m------------------------------\n");
                int i, j;

                for (i = 0; i < kareSayisi; i++) {
                    if (i % 3 == 0)
                        writer.write(" -----------------------\n");

                    for (j = 0; j < kareSayisi; j++) {
                        if (j % 3 == 0) writer.write("| ");
                        if (matris[i][j] == 0)
                            writer.write("* ");
                        else
                            writer.write(Integer.toString(matris[i][j]) + " ");
                    }
                    writer.write("|\n");
                }
                writer.write(" -----------------------\n");
                writer.newLine();
                adimSayisi++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
