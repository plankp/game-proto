package org.atoiks.games.staventure.colliders;

import org.atoiks.games.framework2d.IGraphics;

public final class RectangleCollider implements Collider {

    public float x;
    public float y;
    public float w;
    public float h;

    public RectangleCollider() {
        //
    }

    public RectangleCollider(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Override
    public void render(IGraphics g) {
        g.drawRect(x, y, x + w, y + h);
    }

    @Override
    public boolean collidesWith(Collider other) {
        if (other.getClass() == RectangleCollider.class) {
            final RectangleCollider rect = (RectangleCollider) other;
            return x < rect.x + rect.w && x + w > rect.x
                && y < rect.y + rect.h && y + h > rect.y;
        }
        return false;
    }
}