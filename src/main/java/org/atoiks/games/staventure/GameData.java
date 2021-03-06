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

package org.atoiks.games.staventure;

import java.io.Serializable;

public final class GameData implements Serializable {

    private static final long serialVersionUID = 12834648540L;

    public final StringBuilder portalBuffer = new StringBuilder("Press Enter to unlock then h for help");

    public boolean winAgainstPY = false;

    public void updateState(final GameData newData) {
        portalBuffer.setLength(0);
        portalBuffer.append(newData.portalBuffer);

        winAgainstPY = newData.winAgainstPY;
    }
}
