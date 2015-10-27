/*
 * Copyright (c) 2008, Keith Woodward
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of Keith Woodward nor the names
 *    of its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package straightedge.test.benchmark;

import straightedge.test.benchmark.event.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import straightedge.geom.*;
import java.util.*;

/**
 *
 * @author Keith Woodward
 */
public class GameFrame extends JFrame{
	GameLoop loop;
	ViewPane view;
	JPanel botPanel;
	JCheckBox calcVisionButton;
	JCheckBox recalcPathEveryFrameButton;
	JCheckBox drawNodeConnectorsButton;
	JCheckBox drawGridButton;
	JLabel speedLabel;
	JTextField playerSpeedField;
	JLabel fixedConnectionDistLabel;
	JTextField fixedConnectionDistField;
	JLabel dynamicConnectionDistLabel;
	JTextField dynamicConnectionDistField;
	JLabel gridSizeLabel;
	JTextField gridSizeField;
	JButton reconnectButton;
	JButton newBlankMapButton;
	JButton newMapButton;
	JButton newSmallMazeButton;
	JButton newMediumMazeButton;
	JButton newBigMazeButton;
	
	Object mutex = new Object();
	
    public GameFrame(){
		super("Line of Sight and Path Finding");
		setSize(600, 600);
//		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
			}
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		final GameFrame thisGameFrame = this;
		botPanel = new JPanel();


		calcVisionButton = new JCheckBox("Vision");
		calcVisionButton.setSelected(true);
		calcVisionButton.setToolTipText("<html>If ticked, vision will be calculated every frame.</html>");
		calcVisionButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Player player = thisGameFrame.getLoop().getPlayer();
				int value = (calcVisionButton.isSelected() == true ? 1 : 0);
				PlayerStatusEvent playerStatusEvent = new PlayerStatusEvent(player, thisGameFrame.getLoop().getWorld().getTimeStampForEventNow(), PlayerStatusEvent.PLAYER_CALC_VISION, value);
				player.getView().getEventHandler().addNewEvent(playerStatusEvent);
			}
		});

		recalcPathEveryFrameButton = new JCheckBox("Path");
		recalcPathEveryFrameButton.setSelected(true);
		recalcPathEveryFrameButton.setToolTipText("<html>If ticked, the path will be recalculated every frame rather than only when the path changes. <br>Useful for performance and robustness checking.</html>");
		recalcPathEveryFrameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Player player = thisGameFrame.getLoop().getPlayer();
				int value = (recalcPathEveryFrameButton.isSelected() == true ? 1 : 0);
				PlayerStatusEvent playerStatusEvent = new PlayerStatusEvent(player, thisGameFrame.getLoop().getWorld().getTimeStampForEventNow(), PlayerStatusEvent.PLAYER_RECALC_PATH_EVERY_FRAME, value);
				player.getView().getEventHandler().addNewEvent(playerStatusEvent);
				if (drawNodeConnectorsButton.isSelected() && recalcPathEveryFrameButton.isSelected() == false){
					drawNodeConnectorsButton.doClick();
				}
			}
		});
		drawNodeConnectorsButton = new JCheckBox("Mesh");
		drawNodeConnectorsButton.setSelected(false);
		drawNodeConnectorsButton.setToolTipText("Shows the connections between nodes.");
		drawNodeConnectorsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				thisGameFrame.getLoop().getWorld().setDrawNodeConnections(drawNodeConnectorsButton.isSelected());
				if (drawNodeConnectorsButton.isSelected() && recalcPathEveryFrameButton.isSelected() == false){
					recalcPathEveryFrameButton.doClick();
				}
			}
		});
		drawGridButton = new JCheckBox("Grid");
		drawGridButton.setSelected(false);
		drawGridButton.setToolTipText("Shows the grid used to store obstacles.");
		drawGridButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				thisGameFrame.getLoop().getWorld().setDrawGrid(drawGridButton.isSelected());
			}
		});
		speedLabel = new JLabel("Player speed: ");
		speedLabel.setToolTipText("Type a number in the text field and press enter.");
		playerSpeedField = new JTextField(""+0);
		playerSpeedField.setColumns(5);
		playerSpeedField.setToolTipText("Press enter after typing a number.");
		playerSpeedField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setPlayerSpeedFromTextField();
			}
		});
		playerSpeedField.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				setPlayerSpeedFromTextField();
			}
		});
		fixedConnectionDistLabel = new JLabel("Obstacle max dist: ");
		fixedConnectionDistLabel.setToolTipText("For this to have an effect, click the 'Reconnect' button");
		fixedConnectionDistField = new JTextField(""+600);
		fixedConnectionDistField.setColumns(5);
		fixedConnectionDistField.setToolTipText("For this to have an effect, click the 'Reconnect' button");
		gridSizeLabel = new JLabel("Grid size: ");
		gridSizeLabel.setToolTipText("For this to have an effect, click the 'Reconnect' button");
		gridSizeField = new JTextField(""+150);
		gridSizeField.setColumns(5);
		gridSizeField.setToolTipText("For this to have an effect, click the 'Reconnect' button");
		dynamicConnectionDistLabel = new JLabel("End point max dist: ");
		dynamicConnectionDistLabel.setToolTipText("<html>Max connection distance from the start node or end node to obstacle nodes.<br>Type a number in the text field and press enter.</html>");
		dynamicConnectionDistField = new JTextField(""+600);
		dynamicConnectionDistField.setColumns(5);
		dynamicConnectionDistField.setToolTipText("Press enter after typing a number.");
		dynamicConnectionDistField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setDynamicConnectionDistFromTextField();
			}
		});
		dynamicConnectionDistField.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				setDynamicConnectionDistFromTextField();
			}
		});
		reconnectButton = new JButton("<html><i>Reconnect</i><html>");
		reconnectButton.setToolTipText("Reconnects all obstacle nodes in the current map using the current settings.");
		reconnectButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				restartWithNewWorld(new BlankWorld(thisGameFrame.getLoop().getWorld().getOriginalPolygons()));
			}
		});
		newSmallMazeButton = new JButton("Maze");
		newSmallMazeButton.setToolTipText("Makes a small new random maze.");
		newSmallMazeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				restartWithNewWorld(new MazeWorld(System.currentTimeMillis(), 30, 17, 12, 4f));
			}
		});
		newMediumMazeButton = new JButton("Medium maze");
		newMediumMazeButton.setToolTipText("Makes a medium new random maze.");
		newMediumMazeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//restartWithNewWorld(new MazeWorld(System.currentTimeMillis(), 13, 38, 26, 1f));
				restartWithNewWorld(new MazeWorld(0, 13, 38, 26, 1f));
			}
		});
		newBigMazeButton = new JButton("Pillars");
		newBigMazeButton.setToolTipText("Lots of little obstacles in a circle pattern");
//		newBigMazeButton = new JButton("Gigantic maze!");
//		newBigMazeButton.setToolTipText("<html>Makes a massive new random maze.<br>Warning this could take a while to calculate<br>unless you set 'Obstacle max dist' very low (<40).</html>");
		newBigMazeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				restartWithNewWorld(new PillarWorld());
				//restartWithNewWorld(new MazeWorld(System.currentTimeMillis(), 13, 38, 26, 1f));
				//restartWithNewWorld(new MazeWorld(0, 13, 38, 26, 1f));
				//restartWithNewWorld(new MazeWorld(0, 13, 98, 66, 1f));
				//restartWithNewWorld(new MazeWorld(0, 6, 80, 60, 1f));
				//restartWithNewWorld(new MazeWorld(0, 6, 198, 133, 1f));
			}
		});
		newMapButton = new JButton("Obstacles");
		newMapButton.setToolTipText("Makes a new obstacles map which can be used for performance testing.");
		newMapButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				restartWithNewWorld(new ObstacleWorld());
			}
		});
		//newBlankMapButton = new JButton("Blank");
		//newBlankMapButton.setToolTipText("Makes a new blank map that you can fill with your own obstacles.");
		newBlankMapButton = new JButton("CornerCases");
		newBlankMapButton.setToolTipText("Problematic arrangement of obstacles which cause errors.");
		newBlankMapButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//restartWithNewWorld(new BlankWorld());
				restartWithNewWorld(new CornerCaseWorld());
			}
		});
		JPanel q1 = new JPanel();
		JPanel p1 = new JPanel(new GridLayout(4,1));
		p1.add(speedLabel);
		p1.add(playerSpeedField);
		p1.add(dynamicConnectionDistLabel);
		p1.add(dynamicConnectionDistField);
		q1.add(p1);
		JPanel p2 = new JPanel(new GridLayout(3,1));
		p2.add(calcVisionButton);
		p2.add(recalcPathEveryFrameButton);
		p2.add(drawNodeConnectorsButton);
		//p2.add(drawGridButton);
		q1.add(p2);
		q1.setBorder(new javax.swing.border.TitledBorder("Player and View settings"));
		botPanel.add(q1);
		JPanel q2 = new JPanel();
		q2.setBorder(new javax.swing.border.TitledBorder("Obstacle and Map settings"));
		JPanel p3 = new JPanel(new GridLayout(4,1));
		p3.add(fixedConnectionDistLabel);
		p3.add(fixedConnectionDistField);
		p3.add(gridSizeLabel);
		p3.add(gridSizeField);
		q2.add(p3);
		JPanel p4 = new JPanel(new GridLayout(3,1));
		p4.add(reconnectButton);
		p4.add(newBlankMapButton);
		p4.add(newMapButton);
		q2.add(p4);
		JPanel p5 = new JPanel(new GridLayout(3,1));
		p5.add(newSmallMazeButton);
		p5.add(newMediumMazeButton);
		p5.add(newBigMazeButton);
		q2.add(p5);
		botPanel.add(q2);
		add(botPanel, BorderLayout.SOUTH);
		//restartWithNewWorld(new MazeWorld(0, 13, 38, 26, 1f));
		//restartWithNewWorld(new MazeWorld(0, 30, 17, 12, 4f));
		//restartWithNewWorld(new ObstacleWorld());
		restartWithNewWorld(new MazeWorld(0, 30, 17, 12, 4f));
	}
	boolean firstStart = true;
	public void restartWithNewWorld(final GameWorld newWorld){
		if (loop != null){
			// close the current loop
			loop.close();
		}
		if (view != null){
			// remove the old view
			remove(view);
		}
		// Remake the world. 
		// Note that newWorld.init() can take ages, so we do that in another thread, 
		// not on Swing's Event Dispatch Thread.
		// The below method just calls newWorld.init() after showing a progress dialog.
		showProgressDialog(newWorld);
		Player player = new Player();
		newWorld.addPlayer(player);
		view = new ViewPane();
		loop = new GameLoop(this, newWorld, player, view);
		view = loop.getView();
		newWorld.setLoop(loop);
		// reset the old settings:
		loop.getPlayer().setRecalcPathOnEveryUpdate(recalcPathEveryFrameButton.isSelected());
		loop.getPlayer().setCalcVision(calcVisionButton.isSelected());
		newWorld.setDrawNodeConnections(drawNodeConnectorsButton.isSelected());
		newWorld.setDrawGrid(drawGridButton.isSelected());
		loop.getPlayer().setSpeed(getPlayerSpeedFromTextField());
		loop.getPlayer().setMaxConnectionDist(this.getDynamicConnectionDistFromTextField());
		// add the new view and restart the loop.
		this.add(view, BorderLayout.CENTER);
		this.setVisible(true);
		loop.start();
		if (firstStart){
			JOptionPane.showConfirmDialog(this, "Left click to move the player.\nRight click to remove obstacles.\nRight press, drag then release to insert obstacles.\nTo insert shadow polygons at the mouse position press 'K'.\nHave fun!", "Instructions!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			firstStart = false;
		}
		view.requestFocus();
	}
	
	protected void showProgressDialog(final GameWorld newWorld){
		final JDialog dialog = new JDialog(this);
		dialog.setTitle("Progress");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// Need to implement cancelling the join process mid-way thru.
		dialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				close();
			}
		});
		//CustomProgressPane joinProgressPane = new CustomProgressPane(this, dialog);

		JPanel joinProgressPane = new JPanel();
		{
			JProgressBar progressBar = new JProgressBar();
			JButton cancelButton = new JButton("Quit");
			progressBar.setIndeterminate(true);
			progressBar.setString("   Just a few moments...");
			progressBar.setStringPainted(true);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					dialog.dispose();
					close();
				}
			});
			joinProgressPane.setLayout(new BorderLayout());
			joinProgressPane.add(progressBar);
			joinProgressPane.add(cancelButton, BorderLayout.SOUTH);
		}

		dialog.add(joinProgressPane);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(this);
		synchronized(mutex){
			Thread t = new Thread(new Runnable(){
				public void run(){
					newWorld.init(getMaxConnectionDistFromTextField(), getTileWidthAndHeightFromTextField());
					try{
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
								// Should be done on the EDT.
								dialog.dispose();
							}
						});
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
			});
			t.start();
			dialog.setVisible(true);
		}
	}

	protected float getPlayerSpeedFromTextField(){
		String numString = playerSpeedField.getText();
		boolean invalid = false;
		float number = -1;
		try{
			number = Float.parseFloat(numString);
		}catch(NumberFormatException ex){
			ex.printStackTrace();
		}
		return number;
	}

	protected void setPlayerSpeedFromTextField(){
		float number = getPlayerSpeedFromTextField();
		Player player = getLoop().getPlayer();
		//System.out.println(this.getClass().getSimpleName()+" playerStatusEvent made. player == "+player);
		PlayerStatusEvent playerStatusEvent = new PlayerStatusEvent(player, getLoop().getWorld().getTimeStampForEventNow(), PlayerStatusEvent.PLAYER_SPEED_CHANGE, number);
		player.getView().getEventHandler().addNewEvent(playerStatusEvent);
	}

	protected float getDynamicConnectionDistFromTextField(){
		String numString = dynamicConnectionDistField.getText();
		boolean invalid = false;
		float number = -1;
		try{
			number = Float.parseFloat(numString);
		}catch(NumberFormatException ex){
			ex.printStackTrace();
		}
		return number;
	}
	protected void setDynamicConnectionDistFromTextField(){
		float number = getDynamicConnectionDistFromTextField();
		Player player = getLoop().getPlayer();
		PlayerStatusEvent playerStatusEvent = new PlayerStatusEvent(player, getLoop().getWorld().getTimeStampForEventNow(), PlayerStatusEvent.PLAYER_MAX_CONNECTION_DIST, number);
		player.getView().getEventHandler().addNewEvent(playerStatusEvent);
	}

	protected float getMaxConnectionDistFromTextField(){
		String numString = fixedConnectionDistField.getText();
		float number = 0;
		try{
			number = Float.parseFloat(numString);
		}catch(NumberFormatException ex){
			ex.printStackTrace();
		}
		if (number < 0){
			number = 0;
			fixedConnectionDistField.setText(""+number);
		}

		return number;
	}
	protected float getTileWidthAndHeightFromTextField(){
		String numString = gridSizeField.getText();
		float number = 0;
		try{
			number = Float.parseFloat(numString);
		}catch(NumberFormatException ex){
			ex.printStackTrace();
		}
		if (number < 0){
			number = 1;
			gridSizeField.setText(""+number);
		}
		return number;
	}

	
	public void close(){
		if (loop != null){
			loop.close();
		}
		this.dispose();
		// give everything some time to close, then exit the VM if it hasn't already exited.
		try{Thread.sleep(500);}catch(Exception e){}
		System.exit(0);
	}
	
	public static void main(String[] args) {
		// Should create swing components on Swing's Event Dispatch Thread:
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new GameFrame();
			}
		});
	}

	public GameLoop getLoop() {
		return loop;
	}

	public void setLoop(GameLoop loop) {
		if (this.loop != loop){
			this.loop = loop;
			loop.setFrame(this);
		}
	}
	
	

}
