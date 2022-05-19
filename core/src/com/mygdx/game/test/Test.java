package com.mygdx.game.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

public class Test extends ApplicationAdapter {

	public PerspectiveCamera cam;
	final float[] startPos = {30f, 15f, -25f};

	public Model model;
	public Model model2;
	public ModelInstance instance;
	public ModelInstance instance2;
	public Model platform;
	public ModelInstance platformInstance;
	public ModelBatch modelBatch;
	public Environment environment;

	Texture groundTexture;
	Texture treeTexture;
	Texture woodTexture;
	Texture pineTexture;
	Texture bushTexture;
	TextureRegion treeTextureRegion;
	TextureRegion pineTextureRegion;
	TextureRegion bushTextureRegion;
	Texture skyBox;
	TextureRegion skyBoxRegion;
	Decal tree;
	Decal sky;
	DecalBatch batch;
	List<Decal> trees = new ArrayList<>();

	MeshPartBuilder meshBuilder;

	public CameraInputController camController;

	float[][] ground = new float[1000][1000];
	float[][][] normals = new float[1000][1000][3];

	public Model wheel;
	public ModelInstance wheelInstance;
	public ModelInstance wheelInstance2;

	public Model body;
	public ModelInstance bodyInstance;

	Vector3 axesPosition = new Vector3(18f,10,21);
	Vector3 axesPosition2 = new Vector3(6f,10,21);
	Vector3 axesRotation = new Vector3(0,0,1);
	Vector3 velocity = new Vector3(0,0,0);
	Vector3 velocity2 = new Vector3(0,0,0);

	Wheel wheelPhys = new Wheel("Wheel1",3f,15,axesPosition,axesRotation,velocity, 12f);
	Wheel wheelPhys2 = new Wheel("Wheel2",3f,15,axesPosition2,axesRotation,velocity2, 12f);
	WheelsController wheelsController = new WheelsController(wheelPhys, wheelPhys2);


	@Override
	public void create () {
		skyBox = new Texture("horizon-sky-and-landscape-in-chequamegon-national-forest-wisconsin_800.jpg");
		skyBoxRegion = new TextureRegion(skyBox);

		treeTexture = new Texture("kisspng-spruce-scots-pine-fir-larch-tree-2-in-1-5b1551172e2517.818728531528123671189.png");
		treeTextureRegion = new TextureRegion(treeTexture);

		pineTexture = new Texture("kisspng-scots-pine-tree-clip-art-pine-5abb4fee022fd5.475097571522225134009.png");
		pineTextureRegion = new TextureRegion(pineTexture);

		bushTexture = new Texture("SeekPng.com_desert-bush-png_1091473.png");
		bushTextureRegion = new TextureRegion(bushTexture);

		groundTexture = new Texture("forest-floor-terrain_0010_01_S_enl.jpg");
		groundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		groundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		Material groundMaterial = new Material(new Material(TextureAttribute.createDiffuse(groundTexture)));

		woodTexture = new Texture("wood_planks_new_0001_02_tiled_s.jpg");
		woodTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		woodTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		Material woodMaterial = new Material(new Material(TextureAttribute.createDiffuse(woodTexture)));

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(startPos[0], startPos[1], startPos[2]);
		cam.lookAt(50f, 4f, 1000f);
		cam.near = 1f;
		cam.far = 3000f;
		cam.update();

		batch = new DecalBatch(new CameraGroupStrategy(cam));

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		for(int i=0;i<100;i++){
			for(int j=0;j<200;j++){
				ground[i][j]=groundHeight(i,j);
				float dfdx = groundDx(i,j);
				float dfdy = groundDy(i,j);
				float invSqrt = (float)(1/Math.sqrt(Math.pow(dfdx,2)+Math.pow(dfdy,2)+1))*10f;
				normals[i][j][0]=(-dfdx*invSqrt);
				normals[i][j][1]=(-dfdy*invSqrt);
				normals[i][j][2]=1;
			}
		}

		for (int i=0;i<50;i++){
			tree = Decal.newDecal(treeTextureRegion, true);
			tree.setScale(0.12f);
			float x = 100f*(float)Math.random();
			float z = 50+1000f*(float)Math.random();
			tree.setPosition(x, 40+ ground[Math.round(x/10)][Math.round(z/10)], z);
			trees.add(tree);
		}

		for (int i=0;i<50;i++){
			tree = Decal.newDecal(pineTextureRegion, true);
			tree.setScale(0.032f);
			float x = 100f*(float)Math.random();
			float z = 50+1000f*(float)Math.random();
			tree.setPosition(x, 15+ ground[Math.round(x/10)][Math.round(z/10)], z);
			trees.add(tree);
		}

		for (int i=0;i<200;i++){
			tree = Decal.newDecal(bushTextureRegion, true);
			tree.setScale(0.008f);
			float x = 100f*(float)Math.random();
			float z = 50+1000f*(float)Math.random();
			tree.setPosition(x, 1+ ground[Math.round(x/10)][Math.round(z/10)], z);
			trees.add(tree);
		}

		ModelBuilder modelBuilder = new ModelBuilder();

		////////////////First part of ground
		float u = 0.001f;
		float v = 0.001f;

		modelBuilder.begin();
		meshBuilder = modelBuilder.part("ground1", GL20.GL_TRIANGLES,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal |
						VertexAttributes.Usage.TextureCoordinates, groundMaterial);
		meshBuilder.setUVRange(0,0,100,100);

		for(int i=0;i<99;i++){
			for(int j=0;j<99;j++){
				MeshPartBuilder.VertexInfo v1 = new MeshPartBuilder.VertexInfo().setPos(i,ground[i][j],j).setNor(normals[i][j][0],normals[i][j][1],normals[i][j][2]).setCol(null).setUV((i+1)*u, j*v);
				MeshPartBuilder.VertexInfo v2 = new MeshPartBuilder.VertexInfo().setPos(i,ground[i][j+1],(j+1)).setNor(normals[i][j+1][0],normals[i][j+1][1],normals[i][j+1][2]).setCol(null).setUV(i*v, j*v);
				MeshPartBuilder.VertexInfo v3 = new MeshPartBuilder.VertexInfo().setPos((i+1),ground[i+1][j+1],(j+1)).setNor(normals[i+1][j+1][0],normals[i+1][j+1][1],normals[i+1][j+1][2]).setCol(null).setUV(i*v, (j+1)*v);
				MeshPartBuilder.VertexInfo v4 = new MeshPartBuilder.VertexInfo().setPos((i+1),ground[i+1][j],j).setNor(normals[i+1][j][0],normals[i+1][j][1],normals[i+1][j][2]).setCol(null).setUV((i+1)*u, (j+1)*v);

				meshBuilder.triangle(v1,v2,v4);
				meshBuilder.triangle(v3,v4,v2);
			}
		}

		model = modelBuilder.end();
		instance = new ModelInstance(model);

		////////////////Second part of ground
		modelBuilder.begin();
		meshBuilder = modelBuilder.part("ground2", GL20.GL_TRIANGLES,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal |
						VertexAttributes.Usage.TextureCoordinates, groundMaterial);
		meshBuilder.setUVRange(0,0,100,100);

		for(int i=0;i<99;i++){
			for(int j=100;j<199;j++){
				MeshPartBuilder.VertexInfo v1 = new MeshPartBuilder.VertexInfo().setPos(i,ground[i][j],j).setNor(normals[i][j][0],normals[i][j][1],normals[i][j][2]).setCol(null).setUV((i+1)*u, j*v);
				MeshPartBuilder.VertexInfo v2 = new MeshPartBuilder.VertexInfo().setPos(i,ground[i][j+1],(j+1)).setNor(normals[i][j+1][0],normals[i][j+1][1],normals[i][j+1][2]).setCol(null).setUV(i*v, j*v);
				MeshPartBuilder.VertexInfo v3 = new MeshPartBuilder.VertexInfo().setPos((i+1),ground[i+1][j+1],(j+1)).setNor(normals[i+1][j+1][0],normals[i+1][j+1][1],normals[i+1][j+1][2]).setCol(null).setUV(i*v, (j+1)*v);
				MeshPartBuilder.VertexInfo v4 = new MeshPartBuilder.VertexInfo().setPos((i+1),ground[i+1][j],j).setNor(normals[i+1][j][0],normals[i+1][j][1],normals[i+1][j][2]).setCol(null).setUV((i+1)*u, (j+1)*v);

				meshBuilder.triangle(v1,v2,v4);
				meshBuilder.triangle(v3,v4,v2);
			}
		}

		model2 = modelBuilder.end();
		instance2 = new ModelInstance(model2);

		/////////////////////////Platform

		platform = modelBuilder.createBox(5,0.2f,5,woodMaterial,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal|VertexAttributes.Usage.TextureCoordinates);
		platformInstance = new ModelInstance(platform);
		platformInstance.transform.setToTranslation(53,4,21);


		//////////////////////////////////////

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 10f, 10f, 20f));

		ModelBuilder wheelBuilder = new ModelBuilder();
		wheel = wheelBuilder.createCylinder(4f, 1f, 4f, 100,
				new Material(ColorAttribute.createDiffuse(Color.BLACK)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		wheelInstance = new ModelInstance(wheel);
		wheelInstance.transform.translate(axesPosition);
		wheelInstance2 = new ModelInstance(wheel);
		wheelInstance2.transform.translate(axesPosition2);

		wheelPhys.connectedWheel = wheelPhys2;
		wheelPhys2.connectedWheel = wheelPhys;

		ModelBuilder bodyBuilder = new ModelBuilder();
		body = bodyBuilder.createBox(12,1,0.5f,
				new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		bodyInstance = new ModelInstance(body);
		bodyInstance.transform.translate(axesPosition);
	}

	@Override
	public void render () {
		prepareFrame();
		renderModels();
		renderDecals();
		wheelsController.nextFrame();
		wheelInstance.transform.setToRotation(Vector3.Y,wheelPhys.axesRotation).setTranslation(wheelPhys.axesPosition);
		wheelInstance2.transform.setToRotation(Vector3.Y,wheelPhys2.axesRotation).setTranslation(wheelPhys2.axesPosition);
		Vector3 bodyPosition = new Vector3();
		bodyPosition.set(wheelPhys2.axesPosition);
		bodyPosition.sub(wheelPhys.axesPosition);
		bodyPosition.scl(0.5f);
		bodyPosition.add(wheelPhys.axesPosition);
		Vector3 bodyVector = new Vector3();
		bodyVector.set(wheelPhys.axesPosition);
		bodyVector.sub(wheelPhys2.axesPosition);
		double angle = Math.atan(bodyVector.y/bodyVector.x);
		//bodyInstance.transform.setToRotation(Vector3.Y,Vector3.Y);
		bodyInstance.transform.setToRotationRad(Vector3.Z, (float)angle);
		bodyInstance.transform.setTranslation(bodyPosition);
	}
	
	@Override
	public void dispose () {
		model.dispose();
		wheel.dispose();
		modelBatch.dispose();
		batch.dispose();
	}

	public void prepareFrame(){
		Gdx.graphics.setWindowedMode(1920,1280);
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glCullFace(GL20.GL_NONE);
		camController.update();
	}

	public void renderModels(){
		modelBatch.begin(cam);
		modelBatch.render(instance, environment);
		modelBatch.render(instance2, environment);
		modelBatch.render(wheelInstance, environment);
		modelBatch.render(wheelInstance2, environment);
		//modelBatch.render(platformInstance, environment);
		modelBatch.render(bodyInstance, environment);
		modelBatch.end();
	}

	public void renderDecals(){
		Gdx.gl20.glDepthMask(false);
		for (Decal decal:trees) {
			batch.add(decal);
		}
		sky = Decal.newDecal(skyBoxRegion);
		sky.setScale(10f);
		sky.setPosition(500f, -1000f, 1000f);
		batch.add(sky);
		batch.flush();
		Gdx.gl20.glDepthMask(true);
	}


	public float groundHeight(float x, float y){
		return (float)(Math.sin((x/8)*Math.PI+(y/8)*Math.PI)+2*Math.sin((x/16)*Math.PI-(y/16)*Math.PI));
	}

	public static float groundHeightWithPlatforms(float x, float y){
		//if ((x<=53+2.5f&&x>=53-2.5f)&&(y<=21+2.5f&&y>=21-2.5f)){
		//	return 4+0.1f;
		//}
		return (float)(Math.sin((x/8)*Math.PI+(y/8)*Math.PI)+2*Math.sin((x/16)*Math.PI-(y/16)*Math.PI));
	}

	public float groundDx(float x, float y){
		return (float)((Math.PI/8)*Math.cos((x/8)*Math.PI+(y/8)*Math.PI)+2*(Math.PI/16)*Math.cos((x/16)*Math.PI-(y/16)*Math.PI));
	}

	public float groundDy(float x, float y){
		return (float)((Math.PI/8)*Math.cos((x/8)*Math.PI+(y/8)*Math.PI)+2*(-Math.PI/16)*Math.cos((x/16)*Math.PI-(y/16)*Math.PI));
	}

}
class WheelsController{
	Wheel wheel1;
	Wheel wheel2;
	WheelsController(Wheel wheel1, Wheel wheel2){
		this.wheel1 = wheel1;
		this.wheel2 = wheel2;
	}
	public void nextFrame(){
		wheel1.subFramePositionsWithoutBaseCorrections();
		wheel2.subFramePositionsWithoutBaseCorrections();
		Vector3 gravitationalForce = new Vector3(0,-30f*9f,0);
		Vector3 engineForce = new Vector3(120f,0,0);
		Vector3 force = new Vector3(Vector3.Zero);
		force.add(gravitationalForce);
		force.add(engineForce);
		wheel1.addIncomingForce(force);
		wheel2.addIncomingForce(force);
		wheel1.processCollisionForceFriction(1/30f);
		wheel2.processCollisionForceFriction(1/30f);
		wheel1.processBond();
		wheel2.processBond();
		exchangeImpulses();
		wheel1.setFinalVelocityAndTranslate();
		wheel2.setFinalVelocityAndTranslate();
		applyCorrection();
	}

	public void applyCorrection(){
		Vector3 wheel1AxesPosition = new Vector3();
		Vector3 wheel2AxesPosition = new Vector3();
		wheel1AxesPosition.set(wheel1.axesPosition);
		wheel2AxesPosition.set(wheel2.axesPosition);
		wheel1.axesPosition.set(wheel1.correctedPosition(wheel1AxesPosition, wheel2AxesPosition, 2));
		wheel2.axesPosition.set(wheel2.correctedPosition(wheel2AxesPosition, wheel1AxesPosition, 2));
		wheel1.inCollision = false;
		wheel2.inCollision = false;
	}

	public void exchangeImpulses(){
		Vector3 normalImpulse1 = new Vector3();
		Vector3 normalImpulse2 = new Vector3();
		normalImpulse1.set(wheel1.bondVelocities.get(1));
		normalImpulse2.set(wheel2.bondVelocities.get(1));
		normalImpulse1.scl(0.5f);
		normalImpulse2.scl(0.5f);
		wheel1.bondVelocities.get(1).scl(0.5f);
		wheel2.bondVelocities.get(1).scl(0.5f);
		wheel1.bondVelocities.get(1).add(normalImpulse2);
		wheel2.bondVelocities.get(1).add(normalImpulse1);
	}
}

class Wheel{
	public String name;
	public Vector3 axesPosition = new Vector3(50,10,21);
	public Vector3 oldAxesPosition = new Vector3(50,10,21);
	public Vector3 preCollisionAxesPosition = new Vector3(50,10,21);
	public Vector3 lastCollisionAxesPosition = new Vector3();
	public Vector3 axesRotation = new Vector3(0,0,1);
	public Vector3 velocity = new Vector3(0,0,0);
	public Vector3 velocityIncr = new Vector3(0,0,0);
	public Vector3 acceleration = new Vector3(0,0,0);
	public Vector3 translation = new Vector3(0,0,0);
	public float radius;
	public Vector3 force = new Vector3(Vector3.Zero);
	public float dt;
	public float wheelMass;
	public float friction = 0.98f;
	public List<Vector3> subFramePositionsWithoutBaseCorrections = new ArrayList<>();
	public Wheel connectedWheel;
	public int subFrames = 5;
	public float baseLength;
	List<Vector3> intersectionPoints = new ArrayList<>();
	public List<Vector3> bondVelocities = new ArrayList<>();
	public boolean inCollision = false;


	public void processCollisionForceFriction(float dt){
		this.dt = dt;
		resolveCollision();
		if (intersectionPoints.size()==0) {
			increaseVelocityByExternalForce(dt);
		}
		applyFriction();
	}

	public void processBond(){
		bondVelocities = velocitiesForBond(velocity, oldAxesPosition, connectedWheel.oldAxesPosition);
	}

	public void setFinalVelocityAndTranslate(){
		velocity.set(bondVelocities.get(0));
		velocity.add(bondVelocities.get(1));
		translate();
		resetValues();
	}

	public void translate(){
		translation.set(velocity);
		translation.scl(dt);
		if (intersectionPoints.size() > 0) {
			axesPosition.set(preCollisionAxesPosition);
		}
		oldAxesPosition.set(axesPosition);
		axesPosition.add(translation);
	}

	public void resetValues(){
		force.set(Vector3.Zero);
		intersectionPoints.clear();
	}

	public void applyFriction(){
		if (intersectionPoints.size() == 0) {
			velocity.scl(friction);
		}
	}

	public void resolveCollision(){
		intersectionPoints = wheelGroundIntersectionPoints(oldAxesPosition,axesRotation,radius,subFramePositionsWithoutBaseCorrections);
		Vector3 reactionVector = new Vector3();
		if (intersectionPoints.size()>0){
			inCollision = true;
			reactionVector.set(wheelGroundReactionVector(lastCollisionAxesPosition,intersectionPoints));
			Vector3 oldVelocity = new Vector3();
			oldVelocity.set(velocity);
			velocity.set(velocityAfterCollision(reactionVector,oldVelocity));
		}
	}

	public void increaseVelocityByExternalForce(float dt){
		acceleration.set(force);
		acceleration.scl(1/wheelMass);
		velocityIncr.set(acceleration);
		velocityIncr.scl(dt);
		velocity.add(velocityIncr);
	}

	public void subFramePositionsWithoutBaseCorrections(){
		List<Vector3> subFramePositionsWithoutCorrections = new ArrayList<>();
		for (int i = 0; i <= subFrames; i++){
			Vector3 subTranslation = new Vector3();
			subTranslation.set(translation);
			subTranslation.scl((float)i/subFrames);
			Vector3 subPosition = new Vector3();
			subPosition.set(axesPosition);
			subPosition.add(subTranslation);
			subFramePositionsWithoutCorrections.add(subPosition);
		}
		this.subFramePositionsWithoutBaseCorrections = subFramePositionsWithoutCorrections;
	}

	public Vector3 correctedPosition(Vector3 position1, Vector3 position2, float factor){
		Vector3 newPosition1 = new Vector3();
		newPosition1.set(position1);
		Vector3 positionDiff = new Vector3();
		positionDiff.set(position2);
		positionDiff.sub(position1);
		float baseDeformedLength = positionDiff.len();
		float correction = (baseLength - baseDeformedLength)/factor;
		Vector3 correctionVector = new Vector3();
		if (correction < 0) {
			correctionVector.set(positionDiff);
			correctionVector.scl(1/correctionVector.len()*Math.abs(correction));
		}
		if (correction > 0){
			correctionVector.set(positionDiff);
			correctionVector.scl(-1/correctionVector.len());
			correctionVector.scl(Math.abs(correction));
		}
		newPosition1.add(correctionVector);
		return newPosition1;
	}

	public List<Vector3> tangentialNormalVectors(Vector3 vector, Vector3 centripetalVector){
		List<Vector3> vectors = new ArrayList<>();
		float centripetalVectorLength = centripetalVector.len();
		float normalProjectionLength = vector.dot(centripetalVector)/centripetalVectorLength;
		Vector3 normalVector = new Vector3();
		normalVector.set(centripetalVector);
		normalVector.scl(normalProjectionLength/centripetalVectorLength);
		Vector3 tangentialVector = new Vector3();
		tangentialVector.set(vector);
		tangentialVector.sub(normalVector);
		vectors.add(tangentialVector);
		vectors.add(normalVector);
		return vectors;
	}

	public List<Vector3> velocitiesForBond(Vector3 velocity, Vector3 axesPosition1, Vector3 axesPosition2){
		List<Vector3> orthoVelocities = new ArrayList<>();
		Vector3 centripetalVector = new Vector3();
		centripetalVector.set(axesPosition2);
		centripetalVector.sub(axesPosition1);
		orthoVelocities = tangentialNormalVectors(velocity, centripetalVector);
		return orthoVelocities;
	}

	Wheel(String name, float radius, float wheelMass, Vector3 axesPosition, Vector3 axesRotation, Vector3 velocity, float baseLength){
		this.name = name;
		this.axesPosition = axesPosition;
		this.axesRotation = axesRotation;
		this.velocity = velocity;
		this.radius = radius;
		this.wheelMass = wheelMass;
		this.baseLength = baseLength;
		this.oldAxesPosition = axesPosition;
	}

	public void addIncomingForce(Vector3 force){
		this.force.add(force);
	}

	public List<Vector3> wheelGroundIntersectionPoints(Vector3 oldAxesPosition, Vector3 axesRotation, float radius, List<Vector3> subFramePositions) {
		List<Vector3> intersectionPoints = new ArrayList<>();
		List<Vector3> lastIntersectionPoints = new ArrayList<>();
		Vector3 probeVector = new Vector3();
		probeVector.set(axesRotation);
		probeVector.crs(Vector3.X);
		probeVector.setLength(radius);
		preCollisionAxesPosition.set(oldAxesPosition);
		for(int i=subFramePositions.size()-1; i>=0; i--){
			Vector3 subFramePosition = subFramePositions.get(i);
			intersectionPoints = intersectionPoints(subFramePosition, axesRotation, probeVector);
			if (intersectionPoints.size() > 0){
				lastCollisionAxesPosition.set(subFramePosition);
				lastIntersectionPoints = intersectionPoints;
			}else{
				preCollisionAxesPosition.set(subFramePosition);
				break;
			}
		}
		return lastIntersectionPoints;
	}

	public List<Vector3> intersectionPoints(Vector3 tempAxesPosition, Vector3 axesRotation, Vector3 probeVector){
		List<Vector3> intersectionPoints = new ArrayList<>();
		float dAngle = 1f;
		for (int i = 0; i < 360 / dAngle + 1; i++) {
			probeVector.rotate(axesRotation, dAngle);
			float probeVectorX = probeVector.x + tempAxesPosition.x;
			float probeVectorY = probeVector.y + tempAxesPosition.y;
			float probeVectorZ = probeVector.z + tempAxesPosition.z;
			float groundHeight = Test.groundHeightWithPlatforms(probeVectorX, probeVectorZ);
			if (groundHeight >= probeVectorY) {
				intersectionPoints.add(new Vector3(probeVectorX, probeVectorY, probeVectorZ));
			}
		}
		return intersectionPoints;
	}

	public Vector3 wheelGroundReactionVector (Vector3 axesPosition, List<Vector3> intersectionPoints){
		Vector3 reactionVector = new Vector3(Vector3.Zero);
		for (Vector3 point:intersectionPoints){
			Vector3 centripetalVector = new Vector3();
			centripetalVector.set(axesPosition);
			centripetalVector.sub(point);
			centripetalVector.setLength(1f);
			reactionVector.add(centripetalVector);
		}
		reactionVector.setLength(100f);
		return reactionVector;
	}

	public Vector3 velocityAfterCollision (Vector3 reactionVector, Vector3 velocity){
		Vector3 N = new Vector3();
		N.set(reactionVector);
		Vector3 V = new Vector3();
		V.set(velocity);
		Vector3 newVelocity = new Vector3(Vector3.Zero);
		float projectionLength = Math.abs(V.scl(-1).dot(N)/N.len());
		Vector3 projection = new Vector3();
		projection.set(N);
		projection.scl((1f/N.len())*projectionLength);
		newVelocity.set(projection.scl(2).add(velocity));
		newVelocity.scl(1.0f);
		return newVelocity;
	}
}
