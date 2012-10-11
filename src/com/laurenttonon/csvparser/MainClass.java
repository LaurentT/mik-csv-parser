package com.laurenttonon.csvparser;

import java.awt.Cursor;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainClass extends JFrame {

	/* This Window owns has a worker to copy files in folders */
	class FileCopyWorker extends SwingWorker<Void, Void> {

		private Map<String, List<String>> musicFiles;
		private File directoryOut;
		private FileUtils fileUtils;
		private int nbrProcessed;

		public FileCopyWorker(Map<String, List<String>> musicFiles,
				File directoryOut) {
			super();
			this.musicFiles = musicFiles;
			this.directoryOut = directoryOut;
			this.fileUtils = new FileUtils();
			this.nbrProcessed = 0;
		}

		@Override
		protected Void doInBackground() throws Exception {
				
				for (String key : musicFiles.keySet()) {
					//We first test if the key directory exists under the directoryOut
					String finalPath = directoryOut.getCanonicalPath()+File.separatorChar+key;
					
					File finalDir = new File(finalPath);
					
					if(!finalDir.exists()){
						finalDir.mkdir();
					}
					
					for (String musicFile : musicFiles.get(key)) {
						File fileToCopy = new File(musicFile);
						File fileToCreate = new File(finalDir, fileToCopy.getName());
						txtOutput.append("Copying " + fileToCreate.getAbsolutePath() + "\n");
						if (fileUtils.copyFileToDir(fileToCopy, finalDir)) {
							this.nbrProcessed++;
							setProgress(this.nbrProcessed);
						} else
							setProgress(this.nbrProcessed);
					}
				}

			return null;
		}

		@Override
		protected void done() {
			Toolkit.getDefaultToolkit().beep();
			btnNewButton.setEnabled(true);
			setCursor(null); // turn off the wait cursor
			txtOutput.append("Done!\n");
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtFileToParse;
	private JTextField txtDirectoryOutput;
	private JFileChooser directoryChooser;
	private JFileChooser csvChooser;
	private FileUtils fileUtils;
	private JProgressBar prgBarFiles;
	private JTextArea txtOutput;
	private JButton btnNewButton;

	public MainClass() throws HeadlessException {
		fileUtils = new FileUtils();
		this.initUI();
	}

	private void initUI() {
		final JPanel panel = new JPanel();
		directoryChooser = new JFileChooser();
		csvChooser = new JFileChooser();
		getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblFileToParse = new JLabel("File to parse");
		lblFileToParse.setBounds(10, 11, 100, 30);
		panel.add(lblFileToParse);

		JLabel lblDirectoryOutput = new JLabel("Directory output");
		lblDirectoryOutput.setBounds(10, 52, 100, 30);
		panel.add(lblDirectoryOutput);

		txtFileToParse = new JTextField();
		txtFileToParse.setEditable(false);
		txtFileToParse.setBounds(120, 16, 269, 20);
		panel.add(txtFileToParse);
		txtFileToParse.setColumns(10);

		txtDirectoryOutput = new JTextField();
		txtDirectoryOutput.setEditable(false);
		txtDirectoryOutput.setColumns(10);
		txtDirectoryOutput.setBounds(120, 57, 269, 20);
		panel.add(txtDirectoryOutput);

		JButton btnFileToParse = new JButton("Choose");
		btnFileToParse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileFilter fileFilter = new FileNameExtensionFilter(
						"csv files", "csv", "txt");
				csvChooser.setFileFilter(fileFilter);

				int ret = csvChooser.showDialog(panel, "Select");
				if (ret == JFileChooser.APPROVE_OPTION) {
					txtFileToParse.setText(csvChooser.getSelectedFile()
							.getAbsolutePath());
				}
			}
		});
		btnFileToParse.setBounds(399, 15, 75, 23);
		panel.add(btnFileToParse);

		JButton btnDirectoryOut = new JButton("Choose");
		btnDirectoryOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				directoryChooser
						.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int ret = directoryChooser.showDialog(panel, "Select");
				if (ret == JFileChooser.APPROVE_OPTION) {
					txtDirectoryOutput.setText(directoryChooser
							.getSelectedFile().getAbsolutePath());
				}
			}
		});
		btnDirectoryOut.setBounds(399, 56, 75, 23);
		panel.add(btnDirectoryOut);

		JSeparator separator = new JSeparator();
		separator.setBounds(326, 88, 1, 2);
		panel.add(separator);

		btnNewButton = new JButton("Parse and copy");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Do we know the csv file and where to copy the files?
				if (txtDirectoryOutput.getText().isEmpty()
						|| txtFileToParse.getText().isEmpty()) {
					// TODO Warn the user that at least one field is missing
				} else {
					// Check if file still exists as well as the directory
					File csvFile = csvChooser.getSelectedFile();
					File directoryOut = directoryChooser.getSelectedFile();

					final Map<String, List<String>> musicFiles = fileUtils
							.parseCSV(csvFile, directoryOut);
					int numberOfFiles = 0;

					for (List<String> filesPerKey : musicFiles.values()) {
						numberOfFiles += filesPerKey.size();
					}

					if (musicFiles != null) {

						// Set properties for progress bar
						prgBarFiles.setStringPainted(true);
						prgBarFiles.setMinimum(0);
						prgBarFiles.setMaximum(numberOfFiles);

						// We clear the output
						txtOutput.setText("");

						// We put the cursor in a wait state
						setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));

						// We disable the button
						btnNewButton.setEnabled(false);

						// We create and launch the FileCopyWorker that is going
						// to process and copy files
						final FileCopyWorker worker = new FileCopyWorker(
								musicFiles, directoryOut);

						worker.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getPropertyName() == "progress") {
									prgBarFiles.setValue((Integer) evt
											.getNewValue());
									txtOutput.append(String.format(
											"Completed %d of %d files.\n",
											worker.getProgress(),
											musicFiles.size()));
								}
							}
						});
						worker.execute();
					}
				}
			}
		});
		btnNewButton.setBounds(191, 93, 136, 50);
		panel.add(btnNewButton);

		prgBarFiles = new JProgressBar();
		prgBarFiles.setBounds(10, 154, 464, 20);
		panel.add(prgBarFiles);

		txtOutput = new JTextArea();
		txtOutput.setBounds(10, 185, 464, 147);
		txtOutput.setMargin(new Insets(5,5,5,5));
		txtOutput.setEditable(false);
		panel.add(txtOutput);
		setTitle("CSV Parser");
		setSize(500, 384);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				MainClass mainClass = new MainClass();
				mainClass.setVisible(true);
			}
		});
	}
}
