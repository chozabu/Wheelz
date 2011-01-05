package com.chozabu.android.BikeGame;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.entity.primitive.Polygon;
import org.anddev.andengine.opengl.texture.region.PolygonTextureRegion;

public class ParallaxPoly extends Polygon {
	
	float parallaxX = 1f;
	float parallaxY = 1f;

	public ParallaxPoly(float pX, float pY, float[] pVertices,
			PolygonTextureRegion pTextureRegion) {
		super(pX, pY, pVertices, pTextureRegion);
	}
	public ParallaxPoly(float pX, float pY,float parX, float parY, float[] pVertices,
			 PolygonTextureRegion pTextureRegion) {
		super(pX, pY, pVertices, pTextureRegion);
		setParallaxFactor(parX, parY);
	}

	public void setParallaxFactor(float parX, float parY) {
		parallaxX = parX;
		parallaxY = parY;
	}

	@Override
	protected void applyTranslation(final GL10 pGL) {
		pGL.glTranslatef(this.mX*parallaxX, this.mY*parallaxY, 0);
	}

}
