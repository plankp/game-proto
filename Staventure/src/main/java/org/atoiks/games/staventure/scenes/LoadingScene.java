/**
 *  Staventure
 *  Copyright (C) 2017-2018  Atoiks-Games <atoiks-games@outlook.com>
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
import java.io.InputStream;
import java.io.BufferedInputStream;

import java.awt.Color;
import java.awt.Image;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.imageio.ImageIO;

import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.atoiks.games.framework2d.Scene;
import org.atoiks.games.framework2d.IGraphics;

import org.atoiks.games.staventure.GameData;

import org.atoiks.games.staventure.prefabs.*;

import static org.atoiks.games.staventure.Main.WIDTH;

public final class LoadingScene extends Scene {

    private enum LoadState {
        WAITING, LOADING, DONE, NO_RES;
    }

    private static final Color[] COLORS = {
        Color.red, Color.yellow, Color.green
    };

    private final ExecutorService loader = Executors.newSingleThreadExecutor();

    private LoadState loaded = LoadState.WAITING;

    private float elapsed;

    @Override
    public void resize(int w, int h) {
        // Ignore
    }

    @Override
    public void render(final IGraphics g) {
        g.setClearColor(Color.black);
        g.clearGraphics();

        for (int i = 0; i < COLORS.length; ++i) {
            g.setColor(COLORS[i]);
            final double k = elapsed - 2 * Math.PI * i;
            g.fillCircle((int) (k * 100), (int) (Math.cos(k * 5) * 100) + 300, 30);
        }
    }

    @Override
    public boolean update(final float dt) {
        elapsed += dt;
        if ((elapsed - 2 * Math.PI * COLORS.length) * 100 > WIDTH) {
            elapsed = 0;
        }

        switch (loaded) {
            case NO_RES:
                return false;
            case LOADING:
                break;
            case DONE:
                loader.shutdown();
                scene.gotoNextScene();
                break;
            case WAITING:
                loaded = LoadState.LOADING;
                loader.submit(() -> {

                    // Player resources
                    for (int i = 1; i <= 12; ++i) {
                        final String name = "/main_char/spr_" + i + ".png";
                        loadImageFromResource(name);
                        Player.SPRITE_SHEET[i - 1] = (Image) scene.resources().get(name);
                    }

                    // PY resources
                    for (int i = 1; i <= 12; ++i) {
                        final String name = "/py/spr_" + i + ".png";
                        loadImageFromResource(name);
                        SquashPlayer.SPRITE_SHEET[i - 1] = (Image) scene.resources().get(name);
                    }

                    // SquashCourtScene resources
                    loadImageFromResource("/gym/squash_court/floor.png");

                    // SquashGameScene resources
                    loadImageFromResource("/gym/squash_game/floor.png");

                    // CourtHallwayScene resources
                    loadImageFromResource("/gym/court_hallway/floor.png");

                    // LibraryScene resources
                    loadImageFromResource("/colby/library/floor.png");
                    loadImageFromResource("/colby/library/chair.png");
                    loadImageFromResource("/colby/library/table.png");

                    loadImageFromResource("/colby/library/bookshelf.png");

                    loadImageFromResource("/colby/library/com.png");
                    loadImageFromResource("/colby/library/com_chair.png");
                    loadImageFromResource("/colby/library/com_table.png");

                    loadImageFromResource("/colby/library/office.png");
                    loadImageFromResource("/colby/library/fountain.png");

                    loadImageFromResource("/colby/library/sofa_big.png");
                    loadImageFromResource("/colby/library/sofa_table.png");

                    // ColbyHallwayScene resources
                    loadImageFromResource("/colby/colby_hallway/floor.png");

                    // BusinessOfficeScene resources
                    loadImageFromResource("/colby/business_office/floor.png");
                    loadImageFromResource("/colby/business_office/table.png");
                    loadImageFromResource("/colby/business_office/chair_in.png");
                    loadImageFromResource("/colby/business_office/chair_out.png");
                    loadImageFromResource("/colby/business_office/shelf.png");
                    loadImageFromResource("/colby/business_office/printer.png");

                    // Put a blank game session, see Portals for game state stuff
                    scene.resources().put("save.dat", new GameData());

                    if (loaded == LoadState.LOADING) {
                        loaded = LoadState.DONE;
                    }
                });
                break;
        }

        return true;
    }

    private void loadImageFromResource(final String path) {
        if (loaded != LoadState.LOADING) return;

        final InputStream is = this.getClass().getResourceAsStream(path);
        if (is == null) {
            loaded = LoadState.NO_RES;
            return;
        }
        try {
            scene.resources().put(path, ImageIO.read(is));
        } catch (IOException ex) {
            loaded = LoadState.NO_RES;
        }
    }

    private void loadMusicFromResource(final String path) {
        if (loaded != LoadState.LOADING) return;

        final InputStream is = this.getClass().getResourceAsStream(path);
        if (is == null) {
            loaded = LoadState.NO_RES;
            return;
        }

        // AIS requires the use of BufferedInputStream since res-streams are not buffered
        try (final AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(is))) {
            final Clip clip = AudioSystem.getClip(null);
            clip.open(in);
            clip.stop();
            scene.resources().put(path, clip);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
            loaded = LoadState.NO_RES;
        }
    }
}
