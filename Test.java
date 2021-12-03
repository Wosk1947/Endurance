package com.mygdx.game.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.List;

public class Test extends ApplicationAdapter {

	public PerspectiveCamera cam;
	final float[] startPos = {40f, 8f, -5f};

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
	TextureRegion treeTexture2Region;
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

	public Model marker;
	public ModelInstance markerInstance;
	public ModelInstance markerInstance2;

	Vector3 axesPosition = new Vector3(50f,10,21);
	Vector3 axesPosition2 = new Vector3(38f,10,21);
	Vector3 oldAxesPosition = new Vector3(50,10,21);
	Vector3 preCollisionAxesPosition = new Vector3(50,10,21);
	Vector3 lastCollisionAxesPosition = new Vector3();
	Vector3 axesRotation = new Vector3(0,0,1);
	Vector3 velocity = new Vector3(0,0,0);
	Vector3 velocityIncr = new Vector3(0,0,0);
	Vector3 acceleration = new Vector3(0,0,0);
	Vector3 translation = new Vector3(0,0,0);

	float friction = 1.0f;

	float dt;
	float oldDt;

	Wheel wheelPhys = new Wheel(3f,15,axesPosition,axesRotation,velocity, 12f);
	Wheel wheelPhys2 = new Wheel(2f,15,axesPosition2,axesRotation,velocity, 12f);


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

		for (int i=0;i<500;i++){
			tree = Decal.newDecal(treeTextureRegion, true);
			tree.setScale(0.12f);
			float x = 1000f*(float)Math.random();
			float z = 1000f*(float)Math.random();
			tree.setPosition(x, 40+ ground[Math.round(x/10)][Math.round(z/10)], z);
			trees.add(tree);
		}

		for (int i=0;i<500;i++){
			tree = Decal.newDecal(pineTextureRegion, true);
			tree.setScale(0.032f);
			float x = 1000f*(float)Math.random();
			float z = 1000f*(float)Math.random();
			tree.setPosition(x, 15+ ground[Math.round(x/10)][Math.round(z/10)], z);
			trees.add(tree);
		}

		for (int i=0;i<2000;i++){
			tree = Decal.newDecal(bushTextureRegion, true);
			tree.setScale(0.008f);
			float x = 1000f*(float)Math.random();
			float z = 1000f*(float)Math.random();
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
				new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		wheelInstance = new ModelInstance(wheel);
		wheelInstance.transform.translate(axesPosition);
		wheelInstance2 = new ModelInstance(wheel);
		wheelInstance2.transform.translate(axesPosition2);

		ModelBuilder markerBuilder = new ModelBuilder();
		marker = markerBuilder.createSphere(0.5f, 0.5f, 0.5f, 100, 100,
				new Material(ColorAttribute.createDiffuse(Color.RED)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		markerInstance = new ModelInstance(marker);
		markerInstance2 = new ModelInstance(marker);

		markerInstance.transform.setToTranslation(axesPosition);
		markerInstance2.transform.setToTranslation(axesPosition);


		wheelPhys.connectedWheel = wheelPhys2;
		wheelPhys2.connectedWheel = wheelPhys;

	}

	@Override
	public void render () {
		Gdx.graphics.setWindowedMode(640,480);
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glCullFace(GL20.GL_NONE);

		camController.update();

		modelBatch.begin(cam);
		modelBatch.render(instance, environment);
		modelBatch.render(instance2, environment);
		modelBatch.render(wheelInstance, environment);
		modelBatch.render(wheelInstance2, environment);
		//modelBatch.render(markerInstance, environment);
		//modelBatch.render(markerInstance2, environment);
		modelBatch.render(platformInstance, environment);
		modelBatch.end();

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


		////Wheel physics

		//Parameters
		float dt = Gdx.graphics.getDeltaTime()*1;

		//START

		//Forces
		Vector3 gravitationalForce = new Vector3(0,-15*30,0);

		//Cumulative Force
		Vector3 force = new Vector3(Vector3.Zero);
		force.add(gravitationalForce);

		wheelPhys.addIncomingForce(force);
		//wheelPhys2.addIncomingForce(force);
		wheelPhys.nextFrame(1/30f);
		//wheelPhys2.nextFrame(dt);
		wheelPhys.axesPosition.set(wheelPhys.correctedPosition(wheelPhys.axesPosition,new Vector3(38f,10,21),wheelPhys.baseLength));
		System.out.print(" After Correction Axes: "+wheelPhys.axesPosition+" ");
		System.out.println();
		//wheelPhys2.axesPosition.set(wheelPhys2.correctedPosition(wheelPhys2.axesPosition,wheelPhys.axesPosition,wheelPhys2.baseLength));
		wheelInstance.transform.setToRotation(Vector3.Y,wheelPhys.axesRotation).setTranslation(wheelPhys.axesPosition);
		//wheelInstance2.transform.setToRotation(Vector3.Y,wheelPhys2.axesRotation).setTranslation(wheelPhys2.axesPosition);


	}
	
	@Override
	public void dispose () {
		model.dispose();
		wheel.dispose();
		modelBatch.dispose();
		batch.dispose();
	}


	public float groundHeight(float x, float y){
		return (float)(Math.sin((x/8)*Math.PI+(y/8)*Math.PI)+2*Math.sin((x/16)*Math.PI-(y/16)*Math.PI));
		//return x*0.5f;
	}

	public static float groundHeightWithPlatforms(float x, float y){
		if ((x<=53+2.5f&&x>=53-2.5f)&&(y<=21+2.5f&&y>=21-2.5f)){
			return 4+0.1f;
		}
		return (float)(Math.sin((x/8)*Math.PI+(y/8)*Math.PI)+2*Math.sin((x/16)*Math.PI-(y/16)*Math.PI));
		//return x*0.5f;
	}

	public float groundDx(float x, float y){
		return (float)((Math.PI/8)*Math.cos((x/8)*Math.PI+(y/8)*Math.PI)+2*(Math.PI/16)*Math.cos((x/16)*Math.PI-(y/16)*Math.PI));
	}

	public float groundDy(float x, float y){
		return (float)((Math.PI/8)*Math.cos((x/8)*Math.PI+(y/8)*Math.PI)+2*(-Math.PI/16)*Math.cos((x/16)*Math.PI-(y/16)*Math.PI));
	}

}

class Wheel{
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
	public float oldDt;
	public float dt;
	public float wheelMass;
	public float g=30f;
	public float friction = 1.0f;
	public List<Vector3> subFramePositions = new ArrayList<>();
	public List<Vector3> subFrameRotations = new ArrayList<>();
	public Wheel connectedWheel;
	public int subFrames = 5;
	public float baseLength;

	public void nextFrame(float dt){
		this.dt = dt;

		List<Vector3> subFramePositionsWithoutBaseCorrections1 = subFramePositionsWithoutBaseCorrections(subFrames, translation, oldAxesPosition);
		System.out.print(" SubFrames: "+subFramePositionsWithoutBaseCorrections1);
		//List<Vector3> subFramePositionsWithoutBaseCorrections2 = connectedWheel.subFramePositionsWithoutBaseCorrections(subFrames, connectedWheel.translation, connectedWheel.oldAxesPosition);
		List<Vector3> subFramePositionsWithoutBaseCorrections2 = new ArrayList<>();
		subFramePositionsWithoutBaseCorrections2.add(new Vector3(38f,10,21));
		subFramePositionsWithoutBaseCorrections2.add(new Vector3(38f,10,21));
		subFramePositionsWithoutBaseCorrections2.add(new Vector3(38f,10,21));
		subFramePositionsWithoutBaseCorrections2.add(new Vector3(38f,10,21));
		subFramePositionsWithoutBaseCorrections2.add(new Vector3(38f,10,21));
		subFramePositionsWithoutBaseCorrections2.add(new Vector3(38f,10,21));
		List<Vector3> subFramePositionsWithCorrections = subFramePositionsWithCorrections(subFramePositionsWithoutBaseCorrections1, subFramePositionsWithoutBaseCorrections2, baseLength);
		List<Vector3> intersectionPoints = wheelGroundIntersectionPoints(oldAxesPosition, axesRotation, radius, subFramePositionsWithCorrections);



		/*
		if (intersectionPoints.size()>0) {
			markerInstance.transform.setToTranslation(intersectionPoints.get(0));
			markerInstance2.transform.setToTranslation(intersectionPoints.get(intersectionPoints.size()-1));
		}
		 */

		System.out.print("Velocity before collision: "+velocity+" ");

		//Collision impulse resolution
		Vector3 reactionVector = new Vector3();
		if (intersectionPoints.size()>0){

			reactionVector.set(wheelGroundReactionVector(lastCollisionAxesPosition,intersectionPoints));
			System.out.print("Axis: "+axesPosition+" IntersectionPoints: "+intersectionPoints+" Reaction Vector: "+reactionVector+" ");
			Vector3 oldVelocity = new Vector3();
			oldVelocity.set(velocity);
			velocity.set(velocityAfterCollision(reactionVector,oldVelocity));
		}

		System.out.print("Velocity after collision: "+velocity+" ");


		//Process Force
		/*
		if (intersectionPoints.size()>0){
			Vector3 oldForce = new Vector3();
			oldForce.set(force);
			force.set(tangentForce(reactionVector,oldForce));
		}
		 */



	//	System.out.print("Force: "+force+" DT: "+dt+" ");

		acceleration.set(force);
		acceleration.scl(1/wheelMass);
		velocityIncr.set(acceleration);
		velocityIncr.scl(dt);
		velocity.add(velocityIncr);
		if (intersectionPoints.size()==0) {
			velocity.scl(friction);
		}
//		System.out.print("VelocityIncr: "+velocityIncr+" ");

//		List<Vector3> velocities = velocitiesForBond(velocity, oldAxesPosition, connectedWheel.oldAxesPosition);
		List<Vector3> velocities = velocitiesForBond(velocity, oldAxesPosition, new Vector3(38f,10,21));
		velocities.get(1).scl(0.0f);
		velocities.get(0).scl(velocity.len()/velocities.get(0).len());
		velocity.set(velocities.get(0));
		velocity.add(velocities.get(1));

//		System.out.print("Velocity projections: "+velocities+" ");
//		System.out.print("Final Velocity: "+velocity+" ");


		translation.set(velocity);
		translation.scl(dt);

		System.out.print("Translation: "+translation+" ");

		System.out.print("OldAxes: "+axesPosition+" ");
		System.out.print("PreCollision: "+preCollisionAxesPosition+" ");

		if (intersectionPoints.size() > 0) {
			axesPosition.set(preCollisionAxesPosition);
		}



		oldAxesPosition.set(axesPosition);
		axesPosition.add(translation);

		System.out.print("NewAxes: "+axesPosition+" ");

		//axesPosition.set(correctedPosition(axesPosition,connectedWheel.axesPosition,baseLength));

		force.set(Vector3.Zero);

	}

	public  List<Vector3> subFramePositionsWithoutBaseCorrections(int n, Vector3 translation, Vector3 axesPosition){
		List<Vector3> subFramePositionsWithoutCorrections = new ArrayList<>();
		for (int i = 0; i <= n; i++){
			Vector3 subTranslation = new Vector3();
			subTranslation.set(translation);
			subTranslation.scl((float)i/n);
			Vector3 subPosition = new Vector3();
			subPosition.set(axesPosition);
			subPosition.add(subTranslation);
			subFramePositionsWithoutCorrections.add(subPosition);
		}
		//System.out.print(" POSITIONS SIZE NOT CORRECTED: "+subFramePositionsWithoutCorrections);
		return subFramePositionsWithoutCorrections;
	}

	public  List<Vector3> subFramePositionsWithCorrections(List<Vector3> subFramePositionsWithoutCorrections1, List<Vector3> subFramePositionsWithoutCorrections2, float baseLength){
		List<Vector3> subFramePositionsWithCorrections = new ArrayList<>();
		subFramePositionsWithCorrections.add(subFramePositionsWithoutCorrections1.get(0));
		for (int i = 1; i < subFramePositionsWithoutCorrections1.size(); i++) {
			Vector3 position1 = new Vector3();
			Vector3 position2 = new Vector3();
			position1.set(subFramePositionsWithoutCorrections1.get(i));
			position2.set(subFramePositionsWithoutCorrections2.get(i));
			subFramePositionsWithCorrections.add(correctedPosition(position1, position2, baseLength));
		}
		return subFramePositionsWithCorrections;
	}

	public Vector3 correctedPosition(Vector3 position1, Vector3 position2, float baseLength){
		Vector3 newPosition1 = new Vector3();
		newPosition1.set(position1);
		Vector3 positionDiff = new Vector3();
		positionDiff.set(position2);
		positionDiff.sub(position1);
		float baseDeformedLength = positionDiff.len();
		float correction = (baseLength - baseDeformedLength)/2;
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
		normalVector.scl(normalProjectionLength*1/normalVector.len());
		Vector3 tangentialVector = new Vector3();
		tangentialVector.set(vector);
		tangentialVector.sub(normalVector);
		vectors.add(tangentialVector);
		vectors.add(normalVector);
		return vectors;
	}

	public List<Vector3> forcesForBond(Vector3 force, Vector3 axesPosition1, Vector3 axesPosition2){
		List<Vector3> forces = new ArrayList<>();
		Vector3 centripetalVector = new Vector3();
		centripetalVector.set(axesPosition2);
		centripetalVector.sub(axesPosition1);
		forces = tangentialNormalVectors(force, centripetalVector);
		return forces;
	}

	public List<Vector3> velocitiesForBond(Vector3 velocity, Vector3 axesPosition1, Vector3 axesPosition2){
		List<Vector3> velocities = new ArrayList<>();
		Vector3 centripetalVector = new Vector3();
		centripetalVector.set(axesPosition2);
		centripetalVector.sub(axesPosition1);
		velocities = tangentialNormalVectors(velocity, centripetalVector);
		return velocities;
	}

	Wheel(float radius, float wheelMass, Vector3 axesPosition, Vector3 axesRotation, Vector3 velocity, float baseLength){
		this.axesPosition = axesPosition;
		this.axesRotation = axesRotation;
		this.velocity = velocity;
		this.radius = radius;
		this.wheelMass = wheelMass;
		this.baseLength = baseLength;
	}

	public void addIncomingForce(Vector3 force){
		this.force.add(force);
	}

	public Vector3 outcomingForce(Vector3 point){
		Vector3 outForce = new Vector3();

		return outForce;
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
			System.out.print(" Points size: "+intersectionPoints.size()+" Subframe Num: "+i);
			if (intersectionPoints.size() > 0){
				lastCollisionAxesPosition.set(subFramePosition);
				lastIntersectionPoints = intersectionPoints;
				System.out.print(" Collision SubFrame: "+i);
			}else{
				preCollisionAxesPosition.set(subFramePosition);
				System.out.print(" Break SubFrame: "+i);
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

		System.out.print(" V: "+V+" N: "+ N);

		Vector3 newVelocity = new Vector3(Vector3.Zero);
		float projectionLength = Math.abs(V.scl(-1).dot(N)/N.len());

		System.out.print(" PRLength: "+projectionLength);

		Vector3 projection = new Vector3();
		projection.set(N);
		projection.scl((1f/N.len())*projectionLength);

		System.out.print(" PR: "+projection);

		newVelocity.set(projection.scl(2).add(velocity));

		System.out.print(" newV: "+newVelocity);

		newVelocity.scl(1.0f);
		return newVelocity;
	}

	public Vector3 tangentForce (Vector3 reactionVector, Vector3 force){
		Vector3 N = new Vector3();
		N.set(reactionVector);
		Vector3 F = new Vector3();
		F.set(force);
		Vector3 tangentForce = new Vector3();
		tangentForce.set(F);
		N.scl(-1);
		float projectionLength = N.dot(F)/N.len();
		Vector3 projection = new Vector3();
		projection.set(N);
		projection.setLength(projectionLength);
		tangentForce.sub(projection);
		return tangentForce;
	}
}
