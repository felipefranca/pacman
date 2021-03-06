package pacman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import prolog.Logica;

public final class Maze extends JPanel {
    
    Logica logica = new Logica();
    
    final static int CELL                = 20;
    final int intervaloCriacao           = 50;
    private int      lives               = 1;
    private final String   map           = "src/pacman/levels/level2.txt/";
    private final int      score               = 0;
    private final Ghost    blinky;
    private Cell[][] cells;
    private final Ghost    clyde;
    private final Ghost    inky;
    public Pacman    pacman;
    private final Ghost    pinky;
    private int      tileHeight;
    private int      tileWidth;
    public boolean mortal;
    public boolean ganhou;
    private final Queue<Ghost> deadList  = new ArrayBlockingQueue<>(4);
    private int contador=0;
    private boolean turbo=false;
    private int sleepTurbo=30;
    private int sleepNormal=130;

    public Maze() throws IOException 

    {
        ganhou = false;
        mortal = false;
        createCellArray(map);
        setPreferredSize(new Dimension(CELL * tileWidth, CELL * tileHeight));
        pacman = new Pacman(this, 3,logica);
        inky   = new Ghost(2,this, "inky.png",new IntelForte(logica,2,100),new IntelFraca2(logica,2));
        blinky = new Ghost(0, this, "blinky.png",new IntelForte(logica,0,100), new IntelFraca2(logica,0));
        pinky  = new Ghost(3, this, "pinky.png",new IntelFraca(logica,3,130),new IntelFraca2(logica,3));
        clyde  = new Ghost(1, this, "clyde.png",new IntelFraca(logica,1,130),new IntelFraca2(logica,1));

                
        clyde.matarFantasma();
        inky.matarFantasma();
        pinky.matarFantasma();
      
        deadList.add(inky);
        deadList.add(pinky);
          deadList.add(clyde);
        
        inky.start();
        blinky.start();
        pinky.start();
        clyde.start();
        pacman.start();

        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent k) {
                switch (k.getKeyCode()) {
                case (KeyEvent.VK_KP_DOWN) :
                case (KeyEvent.VK_DOWN) :
                    pacman.setDirection('d');

                    break;

                case (KeyEvent.VK_KP_UP) :
                case (KeyEvent.VK_UP) :
                    pacman.setDirection('u');

                    break;

                case (KeyEvent.VK_KP_RIGHT) :
                case (KeyEvent.VK_RIGHT) : 
                        pacman.setDirection('r');

                    break;

                case (KeyEvent.VK_KP_LEFT) :
                case (KeyEvent.VK_LEFT) :
                    pacman.setDirection('l');
                    break;
                    
                case (KeyEvent.VK_SPACE):
                    if(!turbo)
                    {
                        turbo=true;
                        pacman.setSleep(sleepTurbo);
                    }
                    else
                    {
                        turbo=false;
                        pacman.setSleep(sleepNormal);
                    }

                    break;
                }
            }
        });
       
        repaint();
      
    }

    private void createCellArray(String mapFile) {

        Scanner           fileReader;
        ArrayList<String> lineList = new ArrayList<>();

        try {
            fileReader = new Scanner(new File(mapFile));

            while (true) {
                String line = null;

                try {
                    line = fileReader.nextLine();
                } catch (Exception eof) {}

                if (line == null) {
                    break;
                }

                lineList.add(line);
            }

            tileHeight = lineList.size();
            tileWidth  = lineList.get(0).length();

            cells = new Cell[tileHeight][tileWidth];

            for (int row = 0; row < tileHeight; row++) {
                String line = lineList.get(row);

                for (int column = 0; column < tileWidth; column++) {
                    char type = line.charAt(column);

                    cells[row][column] = new Cell(column, row, type);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Maze map file not found");
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Timer timer = new Timer(); 
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, tileWidth * CELL, tileHeight * CELL);

        if(contador < intervaloCriacao)
            contador++;
        else
        {
            if(!deadList.isEmpty() && !mortal)
            {
                deadList.element().recriarFantasma();
                deadList.remove();
            }
            contador = 0;
        }
        System.out.println(contador);
        
        ganhou = true;
        for (int row = 0; row < tileHeight; row++) {

            for (int column = 0; column < tileWidth; column++) {
                cells[row][column].drawBackground(g);
                if(cells[row][column].getType()=='d' || cells[row][column].getType()=='p')
                {
                    ganhou=false;
                }
                    
            }
        }

       if( checkCollision() && !mortal)
       {    
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Maze.class.getName()).log(Level.SEVERE, null, ex);
            }
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image i = kit.getImage("src/img/large/game_over.png");
           g.drawImage(i, 40, 40, 520, 540,this);
       }
        pacman.drawPacman(g);
        inky.drawGhost(g);
        blinky.drawGhost(g);
        clyde.drawGhost(g);
        pinky.drawGhost(g);
        
        if(ganhou)
        {
            
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image i = kit.getImage("src/img/large/YouWin.PNG");
            g.drawImage(i, 40, 40, 520, 540,this);
            loseLife();
        }        
    }

    public Cell[][] getCells() {
        return cells;
    }

    public int getScore() {
        return pacman.getScore();
    }

    public int getLives() {
        return pacman.getLives();
    }

    public void changeMortal() 
    {
        mortal = !mortal;
        inky.changeIntel();
        blinky.changeIntel();
        pinky.changeIntel();
        clyde.changeIntel();
        
    }


    public boolean checkCollision() 
    {
        
        if(mortal && logica.colisao())
        {
          
            if(logica.colisao1())
            {
              blinky.matarFantasma();
              deadList.add(blinky);
            }
            if(logica.colisao2())
            {
              inky.matarFantasma();
              deadList.add(inky);
            }
            if(logica.colisao3())
            {
              clyde.matarFantasma();
              deadList.add(clyde);
            }
            if(logica.colisao4())
            {
              pinky.matarFantasma();
              deadList.add(pinky);
            }
            return true;
        }
        if(!mortal && (logica.colisao() || verificaTroca(blinky.getMov(),pacman.getMov()) || verificaTroca(pinky.getMov(),pacman.getMov())
                || verificaTroca(inky.getMov(),pacman.getMov()) || verificaTroca(clyde.getMov(),pacman.getMov())))
        {   
            loseLife();
            return true;
        }

        return false; 

    }

    public void loseLife() {
        lives--;

        if (lives <= 0) {
            inky.endgame();
            blinky.endgame();
            pinky.endgame();
            clyde.endgame();
            pacman.endgame();
            System.out.println("Game Over!");
        }
    }
    
    public boolean verificaTroca(Movimento mov1,Movimento mov2)
    {
        return mov1.getX1()==mov2.getX2() && mov1.getY1()==mov2.getY2() && mov1.getX2()== mov2.getX1() && mov1.getY2()==mov2.getY1();
    }
}
