/**
 *  Staventure
 *  Copyright (C) 2017-2019  Atoiks-Games <atoiks-games@outlook.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.atoiks.games.staventure.scenes;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.awt.Color;

import org.atoiks.games.framework2d.Input;
import org.atoiks.games.framework2d.Scene;
import org.atoiks.games.framework2d.KeyCode;
import org.atoiks.games.framework2d.IGraphics;
import org.atoiks.games.framework2d.SceneManager;
import org.atoiks.games.framework2d.ResourceManager;

import org.atoiks.games.framework2d.resource.Font;

import org.atoiks.games.staventure.GameData;

import static org.atoiks.games.staventure.Main.WIDTH;
import static org.atoiks.games.staventure.Main.HEIGHT;

public final class SavePointScene implements Scene {

    private enum Mode {
        INSERT, COMMAND;
    }

    public static final char ESC = '\u001B';

    public static final int MAX_DISPLAY_LINES = 36;

    public final Font VT_FONT;

    private final GameData gameData;

    private final StringBuilder textBuffer;

    private Mode mode = Mode.COMMAND;

    private boolean locked;

    public SavePointScene() {
        this.VT_FONT = ResourceManager.<Font>get("/VT323-Regular.ttf").deriveSize(12.0f);
        this.gameData = ResourceManager.get("./save.dat");
        this.textBuffer = this.gameData.portalBuffer;
    }

    @Override
    public void enter(final Scene from) {
        Input.captureTypedChars(true);
        locked = true;
    }

    @Override
    public void leave() {
        Input.captureTypedChars(false);
    }

    @Override
    public void render(final IGraphics g) {
        g.setClearColor(Color.black);
        g.clearGraphics();

        g.setColor(Color.white);

        final String[] arr = textBuffer.toString().split("\n", -1);
        final int lines = Math.min(arr.length, MAX_DISPLAY_LINES);
        final int offset = Math.max(0, arr.length - MAX_DISPLAY_LINES);
        for (int i = 0; i < lines; ++i) {
            VT_FONT.renderText(g, arr[i + offset], 12, (i + 1) * 12);
        }

        g.setColor(Color.darkGray);
        g.fillRect(0, (MAX_DISPLAY_LINES) * 12 + 6, WIDTH, HEIGHT);

        g.setColor(Color.white);
        VT_FONT.renderText(g, mode.toString(), 12, (MAX_DISPLAY_LINES + 1) * 12 + 3);
    }

    @Override
    public boolean update(final float dt) {
        final char[] arr = Input.getTypedChars().toCharArray();

        if (locked) {
            if (Input.isKeyPressed(KeyCode.KEY_ENTER)) {
                unlockPortal();
            }
            return true;
        }

        switch (mode) {
            case INSERT:    return processInsertMode(arr);
            case COMMAND:   return processCommandMode(arr);
        }
        return true;
    }

    private void unlockPortal() {
        locked = false;
        textBuffer.append("\nWelcome back!\n");
    }

    private boolean processInsertMode(char[] arr) {
        for (final char c : arr) {
            switch (c) {
                case ESC:
                    // Does nothing
                    break;
                case '\b':
                    if (textBuffer.length() > 0) textBuffer.setLength(textBuffer.length() - 1);
                    break;
                case '\t':
                    mode = Mode.COMMAND;
                    break;
                default:
                    textBuffer.append(c);
                    break;
            }
        }
        return true;
    }

    private boolean processCommandMode(char[] arr) {
        for (final char c : arr) {
            switch (c) {
                case 'h':
                    textBuffer
                            .append("\nWelcome to 'The Portal'! This is the storage system used in this game.")
                            .append("\nYou are currently in COMMAND mode (see bottom). By hitting Tab, you can")
                            .append("\nswitch to INSERT mode. Within insert mode, this works like a wimpy text")
                            .append("\neditor. To come back to COMMAND mode, hit Tab again!")
                            .append("\n")
                            .append("\nRemember when you log on to any portal, you need to hit Enter to unlock")
                            .append("\nit. Do not worry. You will not need enter any passcodes!")
                            .append("\n")
                            .append("\nApart from Tab in COMMAND mode, there is also:")
                            .append("\n h - Help, which is this message all again")
                            .append("\n l - Clears screen")
                            .append("\n d - Deletes the last line")
                            .append("\n w - Save the game")
                            .append("\n o - Load a game save")
                            .append("\n p - Reset game")
                            .append("\n q - Close the portal, go back to where you were")
                            .append("\n ! - Close the game")
                            .append("\n")
                            .append("\nHave you played against PY, the squash master, yet?");
                    break;
                case 'l':
                    textBuffer.setLength(0);
                    break;
                case 'd': {
                    final int last = textBuffer.lastIndexOf("\n");
                    if (last >= 0) {
                        textBuffer.delete(last, textBuffer.length());
                    }
                    break;
                }
                case 'w':
                    // Save the game data
                    try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./save.dat"))) {
                        oos.writeObject(gameData);
                        textBuffer.append("\nDone saving game!");
                    } catch (IOException ex) {
                        textBuffer.append("\nFailed to save game: " + ex.getMessage());
                    }
                    break;
                case 'o':
                    // Load a local game save
                    try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./save.dat"))) {
                        gameData.updateState((GameData) ois.readObject());
                        textBuffer.append("\nDone loading game save!");
                    } catch (IOException | ClassNotFoundException ex) {
                        textBuffer.append("\nFailed to load save: " + ex.getMessage());
                    }
                    break;
                case 'p':
                    gameData.updateState(new GameData());
                    unlockPortal();
                    break;
                case 'q':
                    SceneManager.popScene();
                    return true;
                case '!':
                    return false;
                case '\t':
                    mode = Mode.INSERT;
                    break;
            }
        }
        return true;
    }
}
