package main;

import mino.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import mino.Block;
import mino.Mino;
import mino.Long_Bar;
import mino.L_Piece;
import mino.J_Piece;
import mino.O_Block;
import mino.T_Piece;
import mino.Z_Piece;
import mino.S_Piece;

public class PlayManager {

    //Main Play Area
    final int WIDTH = 360;
    final int HEIGHT = 600;

    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;

    //Mino
    Mino currentMino;
    final int MINO_START_X;
    final int MINO_START_Y;
    Mino nextMino;
    final int NEXTMINO_X;
    final int NEXTMINO_Y;
    public static ArrayList<Block> staticBlocks = new ArrayList<>();

    //Others
    public static int dropInterval = 60; //mino drops every 60 frames
    boolean gameOver;

    //Effects
    boolean effectCounterOn;
    int effectCounter;
    ArrayList<Integer> effectY = new ArrayList<>();

    //Score
    int level = 1;
    int lines;
    int score;

    public PlayManager()
    {
        //Main Play Area Frame
        left_x = (GamePanel.WIDTH/2) - (WIDTH/2);
        right_x = left_x + WIDTH;
        top_y = 50;
        bottom_y = top_y + HEIGHT;

        MINO_START_X = left_x + (WIDTH/2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;

        NEXTMINO_X = right_x + 175;
        NEXTMINO_Y = top_y + 500;

        currentMino = pickMino();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        nextMino = pickMino();
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);
    }

    private Mino pickMino()
    {
        //Pick a random mino
        Mino mino = null;
        int i = new Random().nextInt(7);

        switch(i)
        {
            case 0:
                mino = new L_Piece();
                break;

            case 1:
                mino = new J_Piece();
                break;

            case 2:
                mino = new O_Block();
                break;

            case 3:
                mino = new Long_Bar();
                break;

            case 4:
                mino = new T_Piece();
                break;

            case 5:
                mino = new Z_Piece();
                break;

            case 6:
                mino = new S_Piece();
                break;
        }

        return mino;
    }

    public void update()
    {
        //Check if the currentMino is active or still falling
        if(currentMino.active == false)
        {
            //Add into staticBlocks if it hits the floor
            staticBlocks.add(currentMino.b[0]);
            staticBlocks.add(currentMino.b[1]);
            staticBlocks.add(currentMino.b[2]);
            staticBlocks.add(currentMino.b[3]);

            //Check if game over
            if(currentMino.b[0].x == MINO_START_X && currentMino.b[0].y == MINO_START_Y)
            {
                //Checks if current mino collides with existing Mino on the play field
                gameOver = true;
                //GamePanel.music.stop();
                //GamePanel.se.play(2, false);
            }

            currentMino.deactivating = false;

            //Replace current Mino with new one
            currentMino = nextMino;
            currentMino.setXY(MINO_START_X, MINO_START_Y);
            nextMino = pickMino();
            nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);

            checkDelete();
        }
        else
        {
            currentMino.update();
        }
    }

    public void checkDelete()
    {
        int x = left_x;
        int y = top_y;
        int blockCount = 0;
        int lineCount = 0;

        while(x < right_x && y < bottom_y)
        {
            for(int i = 0; i < staticBlocks.size(); i++)
            {
                if(staticBlocks.get(i).x == x && staticBlocks.get(i).y == y)
                {
                    blockCount++;
                }
            }

            x += Block.SIZE;

            if(x == right_x)
            {
                if(blockCount == 12)
                {
                    effectCounterOn = true;
                    effectY.add(y);

                    for(int i = staticBlocks.size() - 1; i > -1; i--)
                    {
                        if(staticBlocks.get(i).y == y)
                        {
                            staticBlocks.remove(i);
                        }
                    }

                    lineCount++;
                    lines++;
                    //If line score hits a certain number, increase drop speed
                    //1 is the fastest
                    if(lines % 10 == 0 && dropInterval > 1)
                    {
                        level++;
                        if(dropInterval > 10)
                        {
                            dropInterval -= 10;
                        }
                        else
                        {
                            dropInterval -= 1;
                        }
                    }

                    for(int i = 0; i < staticBlocks.size(); i++)
                    {
                        if(staticBlocks.get(i).y < y)
                        {
                            staticBlocks.get(i).y += Block.SIZE;
                        }
                    }
                }

                blockCount = 0;
                x = left_x;
                y += Block.SIZE;
            }
        }

        if(lineCount > 0)
        {
            //GamePanel.se.play(1, false);
            int singleLineScore = 10 * level;
            score += singleLineScore * lineCount;
        }
    }

    public void draw(Graphics2D g2)
    {
        //Draw Play Area Frame
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left_x-4, top_y-4, WIDTH+8, HEIGHT+8);

        //Draw Next Mino Frame
        int x = right_x + 100;
        int y = bottom_y - 200;
        g2.drawRect(x, y, 200, 200);
        g2.setFont(new Font("Arial", Font.PLAIN, 30));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString("NEXT", x+60, y+60);

        //Draw Score Frame
        g2.drawRect(x, top_y, 250, 300);
        x += 40;
        y = top_y + 90;
        g2.drawString("LEVEL: " + level, x, y); y+= 70;
        g2.drawString("LINES: " + lines, x, y); y+= 70;
        g2.drawString("SCORE: " + score, x, y);

        //Draw the currentMino
        if(currentMino != null)
        {
            currentMino.draw(g2);
        }
        nextMino.draw(g2);

        for(int i = 0; i < staticBlocks.size(); i++)
        {
            staticBlocks.get(i).draw(g2);
        }

        //Draw Delete Effect
        if(effectCounterOn)
        {
            effectCounter++;

            g2.setColor(Color.red);
            for(int i = 0; i < effectY.size(); i++)
            {
                g2.fillRect(left_x, effectY.get(i), WIDTH, Block.SIZE);
            }

            if(effectCounter == 10)
            {
                effectCounterOn = false;
                effectCounter = 0;
                effectY.clear();
            }
        }


        //Draw Pause and Game Over Screen
        g2.setColor(Color.yellow);
        g2.setFont(g2.getFont().deriveFont(50f));
        if(gameOver)
        {
            x = left_x + 25;
            y = top_y + 320;
            g2.drawString("GAME OVER", x, y);
        }
        else if(KeyHandler.pausePressed)
        {
            x = left_x + 70;
            y = top_y + 320;
            g2.drawString("PAUSED", x, y);
        }

        //Game Title
        x = 35;
        y = top_y + 320;
        g2.setColor(Color.white);
        g2.setFont(new Font("Times New Roman", Font.ITALIC, 60));
        g2.drawString("Simple Tetris", x + 20, y);

    }
}
