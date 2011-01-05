package com.chozabu.android.BikeGame;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.anddev.andengine.opengl.util.GLHelper;


import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.LineJoint;
import com.badlogic.gdx.physics.box2d.joints.LineJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

public class Bike {
	GameWorld gameWorld;
	Sprite mBodyImg;
	public Body mBody;
	private Sprite fWheelImg;
	public Body fWheel;
	private Sprite bWheelImg;
	public Body bWheel;
	public Body roofSensor;
	LineJointDef fJointDef;
	LineJointDef bJointDef;
	LineJoint fJoint;
	LineJoint bJoint;
	private boolean isDead = false;
	

	private float truckLength = 111f;
	private float truckHeight = 49f;
	private float wheelDiam = 42;
	//private final float wheelRad = wheelDiam/2f;
	
	private final float wheelOut = 5;
	private Vector2 fOffset=new Vector2(truckLength-wheelDiam+wheelOut,26);
	private Vector2 bOffset=new Vector2(-wheelOut,26);
	private Vector2 offset=new Vector2(-truckLength/2,-truckHeight-26f/2f);
	public boolean facingRight = true;
	
	private float massMult = 5f;
	private float speedMult = 25f;
	private float spinMult = 1f*massMult;
	//private float torqueMult = 2f*massMult;
	//private float torque = 40*massMult;
	private float suspension = 80*massMult;
	private float suspensionSpeed = 1*massMult;
	float wheelGrip = 5.0f;
	//private boolean facingRight = true;
	public Joint roofJoint;
	
	float eVol = 0.4f;
	private float currentAccel = 0f;
	private float leanForce= 0f;
	//public static final short MASKBITS_CIRCLE = CATEGORYBIT_WALL + CATEGORYBIT_CIRCLE; // Missing: CATEGORYBIT_BOX
	//public static final FixtureDef BIKE_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_CIRCLE, MASKBITS_CIRCLE, (short)0);
	

	SharedPreferences prefs;
	private boolean wheelsAttached = true;

	
	public Bike(GameWorld rootIn,Vector2 startPos){
		startPos.add(offset);
		gameWorld=rootIn;

		prefs = PreferenceManager.getDefaultSharedPreferences(this.gameWorld.root);
		String inStr;
		inStr = prefs.getString("wheelSize", "42");
		//Log.d("ABike","input is: "+inStr);
		wheelDiam = Float.parseFloat(inStr);
		
		PhysicsWorld physicsWorld = gameWorld.mPhysicsWorld;
		
		//Main Body
		this.mBodyImg = new Sprite(startPos.x,startPos.y, truckLength, truckHeight, gameWorld.textures.mTruckBodyTextureRegion) {
			public float altRotation = 0;
			@Override
			protected void applyRotation(final GL10 pGL) {
				/* Disable culling so we can see the backside of this sprite. */
				GLHelper.disableCulling(pGL);
				final float rotation = this.mRotation;
				if(rotation != 0) {
					final float rotationCenterX = this.mRotationCenterX;
					final float rotationCenterY = this.mRotationCenterY;

					pGL.glTranslatef(rotationCenterX, rotationCenterY, 0);
					/* Note we are applying rotation around the y-axis and not the z-axis anymore! */

					pGL.glRotatef(rotation, 0, 0, 1);
					pGL.glRotatef(this.altRotation, 0, 1, 0);
					pGL.glTranslatef(-rotationCenterX, -rotationCenterY, 0);
				}
			}

			@Override
			public void setAlpha(float rots) {
				this.altRotation = rots;
			}
			@Override
			public float getAlpha() {
				return this.altRotation;
			}
			@Override
			protected void drawVertices(final GL10 pGL, final Camera pCamera) {
				//GLHelper.blendFunction(pGL, GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
				//GLHelper.blendFunction(pGL, GL10.GL_ONE, GL10.GL);
				//pGL.glAlphaFunc(GL10.GL_GEQUAL, -1.1f);
				//if (gameWorld.uglyMode) GLHelper.disableBlend(pGL);
				super.drawVertices(pGL, pCamera);

				/* Enable culling as 'normal' entities profit from culling. */
				GLHelper.enableCulling(pGL);
			}
			/*@Override
			protected void applyTranslation(final GL10 pGL) {
				pGL.glTranslatef(this.mX, this.mY, 0);
			}*/
		};

		this.mBodyImg.setUpdatePhysics(false);
		final FixtureDef carFixtureDef = PhysicsFactory.createFixtureDef(1.54f*massMult, 0.3f, 0.4f);
		Rectangle bodyRect = new Rectangle(mBodyImg.getX(), mBodyImg.getY()+truckHeight*0.2f,truckLength,truckHeight*0.6f);
		this.mBody = PhysicsFactory.createBoxBody(physicsWorld, bodyRect, BodyType.DynamicBody, carFixtureDef);
		this.mBody.setUserData(new UserData(this.mBodyImg,null));
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this.mBodyImg, this.mBody, true, true, false, false));
		gameWorld.getScene().getTopLayer().addEntity(this.mBodyImg);
		
		//Roof sensor
		//Sprite roofSensorRect = new Sprite(mBodyImg.getX()-1f+truckLength*0.3f, mBodyImg.getY()-truckHeight*0.1f,truckLength*0.4f,truckHeight*0.2f, gameWorld.textures.mTruckFWheelTextureRegion);
		final FixtureDef roofSensorDef = PhysicsFactory.createFixtureDef(0.1f, 0.3f, 0.7f,true);
		Rectangle roofSensorRect = new Rectangle(mBodyImg.getX()-1f+truckLength*0.3f, mBodyImg.getY()-truckHeight*0.1f,truckLength*0.4f,truckHeight*0.2f);
		this.roofSensor = PhysicsFactory.createBoxBody(physicsWorld, roofSensorRect, BodyType.DynamicBody, roofSensorDef);

		//physicsWorld.registerPhysicsConnector(new PhysicsConnector(roofSensorRect, this.roofSensor, true, true, false, false));
		//gameWorld.getScene().getTopLayer().addEntity(roofSensorRect);
		
		
		//wheel fixture def
		final FixtureDef wheelFixtureDef = PhysicsFactory.createFixtureDef(1.5f*massMult, 0.15f, wheelGrip, false,
				StatStuff.CATEGORYBIT_FG_DYNAMIC, StatStuff.MASKBITS_FG_DYNAMIC, (short)0);
		//front wheel
		this.fWheelImg = new Sprite(startPos.x+fOffset.x,startPos.y+fOffset.y, wheelDiam, wheelDiam, gameWorld.textures.mTruckFWheelTextureRegion);

		this.fWheelImg.setUpdatePhysics(false);
		//final FixtureDef fWheelFixtureDef = PhysicsFactory.createFixtureDef(1.5f*massMult, 0.15f, wheelGrip);
		this.fWheel = PhysicsFactory.createCircleBody(physicsWorld, this.fWheelImg, BodyType.DynamicBody, wheelFixtureDef);
		this.fWheel.setUserData(new UserData(this.fWheelImg,null));
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this.fWheelImg, this.fWheel, true, true, false, false));
		gameWorld.getScene().getTopLayer().addEntity(this.fWheelImg);
		

		//back wheel
		this.bWheelImg = new Sprite(startPos.x+bOffset.x,startPos.y+bOffset.y, wheelDiam, wheelDiam, gameWorld.textures.mTruckBWheelTextureRegion);

		this.bWheelImg.setUpdatePhysics(false);
		//final FixtureDef bWheelFixtureDef = PhysicsFactory.createFixtureDef(1.5f*massMult, 0.15f, wheelGrip);
		this.bWheel = PhysicsFactory.createCircleBody(physicsWorld, this.bWheelImg, BodyType.DynamicBody, wheelFixtureDef);
		this.bWheel.setUserData(new UserData(this.bWheelImg,null));
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this.bWheelImg, this.bWheel, true, true, false, false));
		gameWorld.getScene().getTopLayer().addEntity(this.bWheelImg);
		
		this.fWheel.setAngularDamping(0.5f);
		this.bWheel.setAngularDamping(0.5f);
		
		//roof joint
		WeldJointDef wjd = new WeldJointDef();
		wjd.initialize(mBody, roofSensor, roofSensor.getPosition());
		roofSensor.setUserData(new UserData(this.mBodyImg,null));
		roofJoint = physicsWorld.createJoint(wjd);
		
		
		//int inInt = prefs.getInt("suspensionHeight", 2);
		/*inStr = prefs.getString("suspensionHeight", "2");
		//Log.d("ABike","input is: "+inStr);
		int suspensionHeight = Integer.parseInt(inStr);
		float upperTranslation = 0.01f;
		float lowerTranslation = -0.3f;
		if(suspensionHeight==1){
			upperTranslation = 0.15f;
			lowerTranslation = -0.15f;
		}else if(suspensionHeight==3){
			upperTranslation = -0.15f;
			lowerTranslation = -0.45f;
		}*/
		

		inStr = prefs.getString("suspensionHeight", "0.01");
		//Log.d("ABike","input is: "+inStr);
		float upperTranslation = Float.parseFloat(inStr);
		int check = (int)upperTranslation;
		if(check==upperTranslation){
			Editor editor = prefs.edit();
			editor.remove("suspensionHeight");
			editor.commit();
			upperTranslation = 0.01f;
		}
		inStr = prefs.getString("suspensionLength", "1.0");
		float suspTravel = 0.3f*Float.parseFloat(inStr);
		float lowerTranslation = upperTranslation-suspTravel;

		inStr = prefs.getString("suspensionStiffness", "1.0");
		float suspStiff = Float.parseFloat(inStr);
		
		
		//front/back wheel joints
		//LineJointDef RJD;
		this.fJointDef = new LineJointDef();
		this.fJointDef.initialize(mBody, fWheel, fWheel.getPosition(), new Vector2(0,1));
		this.fJointDef.enableMotor = true;
		this.fJointDef.upperTranslation = upperTranslation;
		this.fJointDef.lowerTranslation = lowerTranslation;
		this.fJointDef.enableLimit = true;
		this.fJointDef.motorSpeed = suspensionSpeed*suspStiff;
		this.fJointDef.maxMotorForce = suspension*suspStiff;
		this.fJoint = (LineJoint) physicsWorld.createJoint(this.fJointDef);
		
		this.bJointDef = new LineJointDef();
		this.bJointDef.initialize(mBody, bWheel, bWheel.getPosition(), new Vector2(0,1));
		this.bJointDef.enableMotor = true;
		this.bJointDef.upperTranslation = upperTranslation;
		this.bJointDef.lowerTranslation = lowerTranslation;
		this.bJointDef.enableLimit = true;
		this.bJointDef.motorSpeed = suspensionSpeed*suspStiff;
		this.bJointDef.maxMotorForce = suspension*suspStiff;
		this.bJoint = (LineJoint) physicsWorld.createJoint(this.bJointDef);
		//detachWheels();
		
		
		 
	}
	
	public void frameUpdate(float pSecondsElapsed){
		calcERate();
		powerWheels();

		//mBody.applyAngularImpulse(this.leanForce);
		mBody.applyTorque(this.leanForce*60);
	}

	private void powerWheels(){
		float fwspeed = this.fWheel.getAngularVelocity();
		float bwspeed = this.bWheel.getAngularVelocity();
		if (fwspeed<0)fwspeed=-fwspeed;
		if (bwspeed<0)bwspeed=-bwspeed;
		if((currentAccel<0 && facingRight)||(currentAccel>0 && !facingRight)){
			//if(bwspeed>2 || fwspeed>2){
				this.fWheel.setAngularVelocity(0f);
				this.bWheel.setAngularVelocity(0f);
				return;
			//}
		}else{
		float fwDiv = 1;
		float bwDiv = 1;
		if (fwspeed>11)fwDiv=fwspeed-10;
		if (bwspeed>11)bwDiv=bwspeed-10;
		if(fwspeed<50)this.fWheel.applyTorque(currentAccel*0.6f/fwDiv*60);
		if(bwspeed<50)this.bWheel.applyTorque(currentAccel*0.6f/bwDiv*60);
		}
	}
	private void calcERate(){
		float espeed = this.bWheel.getAngularVelocity()*0.07f;
		if (espeed<0)espeed=-espeed;
		espeed = (float)Math.sqrt(espeed);
		espeed+=0.4f;
		gameWorld.sounds.mEngineSound.setRate(espeed);
	}

	public void stopWheels(){
		this.bWheel.setAngularVelocity(0f);
		this.fWheel.setAngularVelocity(0f);
	}
	public void flipDirecion(){
		LineJoint temp = this.bJoint;
		this.bJoint = this.fJoint;
		this.fJoint = temp;
		
		Body tempBody = this.bWheel;
		this.bWheel = this.fWheel;
		this.fWheel = tempBody;
		
		facingRight=!facingRight;
		if(facingRight)
			this.mBodyImg.setAlpha(0);//overridden to provide 3d rotation
		else
			this.mBodyImg.setAlpha(180);

		currentAccel=-currentAccel;
		//setSpeed(this.currentAccel/this.speedMult);
		//this.mBodyImg.setScaleX(-this.mBodyImg.getScaleX());
		//this.mBodyImg.setAltRot
	}
	
	void calcVol(float inSpeed){
		if(inSpeed<0)inSpeed=-inSpeed;
		eVol=(eVol*0.6f)+(inSpeed*0.5f+.5f)*.4f;
		gameWorld.sounds.mEngineSound.setVolume(eVol);
	}
	
	public void setSpeed(float inSpeed){
		if(isDead)return;

		if(inSpeed>=0){
			if(inSpeed>0.1)inSpeed-=0.1;
			else {
				//enableMotor = false;
				//return;
			}
		} else {
			if(inSpeed<-0.1)inSpeed+=0.1;
			else{
				//enableMotor = false;
				//return;
			}
			//inSpeed*=0.4f;
		}
		calcVol(inSpeed);
		//if (inSpeed < 3 && inSpeed > -3) enableMotor = false;
		inSpeed*=speedMult;
		if(!facingRight)inSpeed=-inSpeed;
		this.currentAccel = inSpeed;
		//if(this.bJoint.getMotorSpeed()!=inSpeed)
		//	Log.i("ABike", "speed set to: "+inSpeed);
		//this.bJoint.setMotorSpeed(inSpeed);
		//this.bJoint.enableMotor(enableMotor);
		//this.fJoint.setMotorSpeed(inSpeed);
		//this.fJoint.enableMotor(enableMotor);
		
	}
	public boolean isDead(){
		return isDead;
	}
	public void setDead(boolean pDead){
		this.setSpeed(0);
		this.modRot(0);
		isDead = pDead;
	}
	public boolean containsBody(Body body){
		if (body == this.mBody
				|| body == this.fWheel
				|| body == this.bWheel
				|| body == this.roofSensor) return true;
		return false; 
	}

	public void modRot(float force){
		if(isDead)return;
		this.leanForce=force*spinMult;

		//force*=spinMult;
		//mBody.applyAngularImpulse(force);
	}
	

	void setPos(Vector2 inVeca){
		Vector2 inVec = inVeca.cpy();
		inVec.mul(1.f/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
		inVec.sub(this.mBody.getPosition());
		Vector2 pos;
		float angle;
		pos = this.mBody.getPosition().add(inVec);
		angle = this.mBody.getAngle();
		this.mBody.setTransform(pos, angle);
		

		pos = this.fWheel.getPosition().add(inVec);
		angle = this.fWheel.getAngle();
		this.fWheel.setTransform(pos, angle);
		
		pos = this.bWheel.getPosition().add(inVec);
		angle = this.bWheel.getAngle();
		this.bWheel.setTransform(pos, angle);
		//this.bWheel.setLinearVelocity(new Vector2(0,0));
	}

	public void attachWheels() {
		/*gameWorld.root.getEngine().runOnUpdateThread(new Runnable() {
			@Override
			public void run() {*/
		if(wheelsAttached)return;
		wheelsAttached = true;
		fJoint = (LineJoint) gameWorld.mPhysicsWorld.createJoint(fJointDef);
		bJoint = (LineJoint) gameWorld.mPhysicsWorld.createJoint(bJointDef);
		//	}});
		
	}
	public void detachWheels() {
		gameWorld.root.getEngine().runOnUpdateThread(new Runnable() {
			@Override
			public void run() {

		if(!wheelsAttached)return;
		wheelsAttached = false;
		gameWorld.mPhysicsWorld.destroyJoint(fJoint);
		fJoint = null;
		gameWorld.mPhysicsWorld.destroyJoint(bJoint);
		bJoint = null;

			}
		});
		
	}
	
	

}
