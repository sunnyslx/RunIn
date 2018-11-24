package com.steve.runintest.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Celestial {
	private final float UNIT_SIZE=50.0f;
	private FloatBuffer   mVertexBuffer;
	private IntBuffer   mColorBuffer;
    private int vCount=0;
    float yAngle;
    private int xOffset;
    private int zOffset;
    private float scale;
    public Celestial(int xOffset,int zOffset,float scale,float yAngle,int vCount)
    {
    	this.xOffset=xOffset;
    	this.zOffset=zOffset;
    	this.yAngle=yAngle;
    	this.scale=scale;
    	this.vCount=vCount;
        float vertices[]=new float[vCount*3];
        for(int i=0;i<vCount;i++)
        {
        	double angleTempJD=Math.PI*2*Math.random();
        	double angleTempWD=Math.PI/2*Math.random();
        	vertices[i*3]=(float)(UNIT_SIZE*Math.cos(angleTempWD)*Math.sin(angleTempJD));
        	vertices[i*3+1]=(float)(UNIT_SIZE*Math.sin(angleTempWD));
        	vertices[i*3+2]=(float)(UNIT_SIZE*Math.cos(angleTempWD)*Math.cos(angleTempJD));
        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
       //================begin============================
        final int one = 65535;
        int colors[]=new int[vCount*4];
        for(int i=0;i<vCount;i++)
        {
        	colors[i*4]=one;
        	colors[i*4+1]=one;
        	colors[i*4+2]=one;
        	colors[i*4+3]=0;
        }
        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asIntBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);
        //================end============================
    }

    public void drawSelf(GL10 gl)
    {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisable(GL10.GL_LIGHTING);
        gl.glPointSize(scale);
        gl.glPushMatrix();
        gl.glTranslatef(xOffset*UNIT_SIZE, 0, 0);
        gl.glTranslatef(0, 0, zOffset*UNIT_SIZE);
        gl.glRotatef(yAngle, 0, 1, 0);
        gl.glVertexPointer
        (
        		3,
        		GL10.GL_FLOAT,
        		0,
        		mVertexBuffer
        );
        gl.glColorPointer
        (
        		4,
        		GL10.GL_FIXED,
        		0,
        		mColorBuffer
        );
        gl.glDrawArrays
        (
        		GL10.GL_POINTS,
        		0,
        		vCount
        );
        gl.glPopMatrix();
        gl.glPointSize(1);
        gl.glDisable(GL10.GL_LIGHTING);
    }
}
