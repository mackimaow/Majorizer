package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import framework.Course;
import framework.Curriculum;
import framework.DatabaseManager;
import framework.Majorizer;
import framework.Request;
import framework.Student;
import framework.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import utils.ResourceLoader;
import utils.Single;

public class UserInterface extends Application{

	public static final int NO_SEMESTER_SELECTED = -1;
	
	
	
	private Scene scene = null;
	private final GridPane semesters[] = new GridPane[8];
	private int selectedSemester;
	
	private final GridPane currentSelectedSemesterTab = newActionGrid();
	Dimension screenSize;
	boolean isAuthenticated = false;
	
	private ArrayList<Course> addedCourses = new ArrayList<Course>();		//TODO:this
	private ArrayList<Course> droppedCourses = new ArrayList<Course>();
	private ArrayList<Course> xCourses = new ArrayList<Course>();
	private ArrayList<Course> searchedCourses = new ArrayList<Course>();		//Necessary to do as global? --probably
	
	public void updateUI()	{
		
		Node root = null;

		this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		//Login Screen
		if(!isAuthenticated)
			root = loginScreen();
		else	{
			if(Majorizer.getUser().isUserIsStudent())
				root = studentView();
			else
				root = advisorView();
		}
		
		if(scene == null)
			scene = new Scene((Parent) root);
		else
			scene.setRoot((Parent) root);
		
		try {
			String path = ResourceLoader.getCSSPath("application.css");
			scene.getStylesheets().add(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public String getName()	{
		return Majorizer.getStudent().getFirstName() + ' ' + Majorizer.getStudent().getLastName();
	}
	
	public String getNameAdvisor() {
		return Majorizer.getAdvisor().getFirstName() + ' ' + Majorizer.getAdvisor().getLastName();
	}

	public String getStudentID()	{
		return Majorizer.getStudent().getUniversityID();
	}

	public void loginPressed(ActionEvent action) {
		System.out.println("Here");
	}
	
	public void newSemester(final int semesterNumber, final String css)	{
		GridPane semester = new GridPane();
		semester.setMinSize((screenSize.getWidth()*(2/3.0))/8.0, screenSize.getHeight()/5.0);
		semester.getStyleClass().add(css);
		semester.setStyle("-fx-border-color: black; -fx-border-width: 1px; outline: solid;");
		semester.setOnMouseClicked((me) -> { 
			selectSemester(semesterNumber);
		});
		this.semesters[semesterNumber] = semester;
	}
	
	public void selectSemester(int semesterNumber)	{
		selectedSemester = semesterNumber;
		currentSelectedSemesterTab.getChildren().clear();
		for(int semester = 0; semester <= 7; ++semester)	{
			if(semester == semesterNumber)	{
				this.semesters[semester].setStyle("-fx-border-color: yellow; -fx-border-width: 3px; outline: solid;");
				populateSemesterShown(semester);
			}
			else	{
				this.semesters[semester].setStyle("-fx-border-color: black; -fx-border-width: 1px; outline: solid;");
				
			}
		}
	}

	public void populateSemesterShown(int semester)	{
		ArrayList<Integer> currentSemCourses = getCurrentSemesterCourses();
		
		for(int courseIdx = 0; courseIdx < currentSemCourses.size(); ++courseIdx)	{
			final int courseID = currentSemCourses.get(courseIdx);
			Course course = DatabaseManager.getCourse(courseID);
			addCourseToCurrentSemester(course);
		}
	}
	
	private synchronized void addCourseToCurrentSemester(Course c) {
		if(selectedSemester == NO_SEMESTER_SELECTED)
			return;
		
		String courseName = c.getCourseName();
		String courseCode = c.getCourseCode();
		final int courseID = c.getCourseID();
		
		int currentSemester = Majorizer.getStudentCurrentSemesterIndex();

		final Label cLabel = new Label(courseCode + '\t' + courseName);
		
		final ObservableList<Node> nodes = currentSelectedSemesterTab.getChildren();
		
		if(selectedSemester < currentSemester) {
			int numElements = nodes.size();
			currentSelectedSemesterTab.add(cLabel, 0, numElements);
		} else {
			int numElements = nodes.size() / 2;
			currentSelectedSemesterTab.add(cLabel, 0, numElements);
			final Button removeCourseButton = newRemoveButton();
			currentSelectedSemesterTab.add(removeCourseButton, 1, numElements);

			removeCourseButton.setOnMouseClicked((me) -> {
				Iterator<Integer> iterator = getCurrentSemesterCourses().iterator();
				while(iterator.hasNext()) {
					Integer id = iterator.next();
					if(id == courseID) {
						iterator.remove();
						break;
					}
				}
				nodes.remove(cLabel);
				nodes.remove(removeCourseButton);
			});
		}
	}
	
	private ArrayList<Integer> getCurrentSemesterCourses() {
		if(selectedSemester == NO_SEMESTER_SELECTED)
			return new ArrayList<>();
		Map<String, ArrayList<Integer>> courses = Majorizer.getStudent().getAcademicPlan().getSelectedCourseIDs();
		String selectedSemesterString = Majorizer.getStudentCurrentSemesterString(selectedSemester);
		ArrayList<Integer> semCourses = courses.get(selectedSemesterString);
		if(semCourses == null) {
			synchronized (this) {
				semCourses = new ArrayList<Integer>();
				courses.put(selectedSemesterString, semCourses);
			}
		}
		return semCourses;
	}
	

//REUSABLE ELEMENTS===========================
	public Button newRemoveButton()	{
		Button removeButton = new Button();
		removeButton.setShape(new Circle(2));
		ImageView redMinusMark = null;
		try {
			redMinusMark = new ImageView(ResourceLoader.getImage("minus-512.png"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		removeButton.setGraphic(redMinusMark);
		redMinusMark.setFitWidth(10);
		redMinusMark.setFitHeight(25);
		removeButton.getStyleClass().add("redbuttontheme");

		return removeButton;
	}
	
	public Button newAddButton()	{
		Button addButton = new Button();
		addButton.setShape(new Circle(2));
		ImageView greenPlusMark = null;
		try {
			greenPlusMark = new ImageView(ResourceLoader.getImage("NEWgreenPlus.png"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		addButton.setGraphic(greenPlusMark);
		greenPlusMark.setFitWidth(30);
		greenPlusMark.setFitHeight(20);
		addButton.getStyleClass().add("greenbuttontheme");
		return addButton;
	}

	public GridPane newActionGrid()	{
		GridPane actionGrid = new GridPane();
		actionGrid.setHgap(5);
		actionGrid.setPadding(new Insets(5));
		actionGrid.setMinSize(180, 200);		//fields?
		return actionGrid;
	}
	
	public ScrollPane newActionScroll()	{
		ScrollPane actionScroll = new ScrollPane();
		actionScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		actionScroll.setPrefViewportHeight(200);
		actionScroll.getStyleClass().add("windows");
		return actionScroll;
	}
	
	public Label newStyledLabel(String text, String css)	{
		Label styledLabel = new Label(text);
		styledLabel.getStyleClass().add(css);
		return styledLabel;
	}
	
	public Button newLogButton(String text)	{
		Button logButton = new Button();
		logButton.setShape(new Rectangle());
		logButton.setText(text);
		logButton.getStyleClass().add("logoutbuttontheme");
		return logButton;
	}
//===================================================================
	
	public BorderPane loginScreen()	{
		BorderPane loginScreen = new BorderPane();
		try	{
			loginScreen.getStyleClass().add("goldgradient");
			
			VBox login = new VBox();
			login.getStyleClass().add("roundedRect");
			login.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.DARKGOLDENROD, 3, 0, -3, -3));
			
			
			login.setMaxSize(400, 300);
			
			VBox titleBox = new VBox();
			Label title = new Label("Majorizer");
			titleBox.getChildren().add(title);
			title.getStyleClass().add("font");
			titleBox.setAlignment(Pos.CENTER);
			titleBox.setPadding(new Insets(10, 50, 10, 50));
			
			GridPane loginGrid = new GridPane();
			
			TextField username = new TextField();
			username.setPromptText("Username");
			username.getStyleClass().add("lightgraytheme");
			username.getStyleClass().add("boxoutline");
			TextField password = new PasswordField();
			password.setPromptText("Password");
			password.getStyleClass().add("lightgraytheme");
			password.getStyleClass().add("boxoutline");
			Label ulabel = newStyledLabel("Username", "lightgraytheme");
			Label plabel = newStyledLabel("Password", "lightgraytheme");
			
			//Login Button
			Button loginButton = newLogButton("Log In");
			
			loginButton.setDefaultButton(true);
			
			VBox loginButtonBox = new VBox();
			loginButtonBox.getChildren().add(loginButton);
			loginButtonBox.setAlignment(Pos.CENTER);
			loginButtonBox.setPadding(new Insets(5));
			loginButtonBox.getStyleClass().add("logoutbuttontheme");
			
			loginGrid.add(ulabel, 0, 1);
			loginGrid.add(username, 1, 1);
			loginGrid.add(plabel, 0, 2);
			loginGrid.add(password, 1, 2);
			loginGrid.add(loginButtonBox, 1, 3);
			loginGrid.setAlignment(Pos.CENTER);
			
			loginGrid.setPadding(new Insets(20, 20, 20, 20));
			loginGrid.setHgap(5);
			loginGrid.setVgap(1);
			
			login.getChildren().add(titleBox);
			login.getChildren().add(loginGrid);
		
			
	    	 Platform.runLater(() -> {
	    	        if (!username.isFocused()) {
	    	            username.requestFocus();
	    	        }
	    	    });
	    	 
	    	
	    	loginButton.setOnAction((ae) -> {
	    		User user = Majorizer.authenticate(username.getText(), password.getText());
	    		Majorizer.setUser(Majorizer.authenticate(username.getText(), password.getText()));
	    		if(Majorizer.getUser() != null)
	    			this.isAuthenticated = true;
	    		this.updateUI();
	    	});
	    	
	    	
	    	VBox logoBox = new VBox();
	    	ImageView logoView = new ImageView();
	    	Image logo = ResourceLoader.getImage("logoGreen2.png");
	    	logoView.setPreserveRatio(true);
	    	logoView.setFitWidth(600);
	    	logoView.setImage(logo);
	    	logoBox.getChildren().add(logoView);
	    	logoBox.setMaxHeight(100);
	    	logoBox.setPadding(new Insets(20));
	    	
			loginScreen.setCenter(login);
			loginScreen.setTop(logoBox);
			
			GridPane.setMargin(login, new Insets(100));

		}	catch( IOException ioe)	{
			ioe.printStackTrace();
		}
		return loginScreen;
	}
	
	public BorderPane studentView()	{

		
		BorderPane studentScreen = new BorderPane();
		try	{
			studentScreen.getStyleClass().add("lightgraytheme");
			
			
			
			ColumnConstraints constraints = new ColumnConstraints();
			constraints.setHgrow(Priority.ALWAYS);

			GridPane orgPane = new GridPane();
			GridPane topPane = new GridPane();
			GridPane actionPane = new GridPane();
			GridPane searchPane = new GridPane();
			
			Label name = new Label();
			name.setText(getName());
			name.getStyleClass().add("fonttitle");
			
			Label studentID = new Label();
			studentID.setText(getStudentID()); 		//Likewise ^^
			studentID.getStyleClass().add("IDfont");

			//Logout Button
			Button logoutButton = newLogButton("Log Out");
			logoutButton.setOnAction((ae) -> {
				Platform.exit();
			});
			
			VBox logoutButtonBox = new VBox();
			logoutButtonBox.getChildren().add(logoutButton);
			logoutButtonBox.setAlignment(Pos.TOP_RIGHT);
			logoutButtonBox.setPadding(new Insets(5));
			logoutButtonBox.getStyleClass().add("logoutbuttontheme");
			
			topPane.add(name, 0, 0);
			topPane.add(studentID, 0, 1);
			topPane.add(logoutButtonBox, 1, 0);
			GridPane.setMargin(logoutButtonBox, new Insets(20, 0, 0, 0));
			topPane.getColumnConstraints().add(constraints);
			
			//Schedule Pane
			GridPane schedulePane = new GridPane();
			
			//Header label for Schedule Pane
			Label header = newStyledLabel("Course Schedule", "fonttitle");
			schedulePane.add(header, 0, 0);
			
			//Schedule
			GridPane scheduleBox = new GridPane();
			for(int columnIndex = 0; columnIndex <= 7; ++columnIndex)	{
				if(columnIndex < Majorizer.getStudentCurrentSemesterIndex())
					newSemester(columnIndex, "pastSem");
				else if(columnIndex == Majorizer.getStudentCurrentSemesterIndex())
					newSemester(columnIndex, "currentSem");
				else if(columnIndex > Majorizer.getStudentCurrentSemesterIndex())
					newSemester(columnIndex, "futureSem");
				
				scheduleBox.add(this.semesters[columnIndex], columnIndex, 1);
			}
			schedulePane.add(scheduleBox, 0, 1);
			
			final double tabWidth = 1/2.0;
			
			actionPane.setPrefWidth((screenSize.getWidth()*(2/3.0)));
//			actionPane.getColumnConstraints().add(constraints);
			
			//Majors and Minors Pane
			GridPane curriculumPane = new GridPane();
			curriculumPane.setMaxWidth(screenSize.getWidth()*(2/3.0)*tabWidth-50.0);
			curriculumPane.setMinWidth(screenSize.getWidth()*(2/3.0)*tabWidth-50.0);
			
			//Header label for Majors and Minors
			Label curriculumHeader = newStyledLabel("Majors and Minors", "fontmed");
			
			//Green Plus Button for Majors and Minors
			Button addCurriculumButton = newAddButton();
			
			GridPane curriculumTab = newActionGrid();
			ScrollPane curriculumScroll = newActionScroll();
			curriculumScroll.setContent(curriculumTab);
			curriculumScroll.setMinViewportWidth(curriculumTab.getWidth());
			
			curriculumPane.add(curriculumHeader, 0, 0);
			curriculumPane.add(addCurriculumButton, 1, 0);
			curriculumPane.add(curriculumScroll, 0, 1);
			
			ArrayList<Integer> curriculae = Majorizer.getStudent().getAcademicPlan().getDegreeIDs();
			for(int curriculumIdx = 0; curriculumIdx < curriculae.size(); ++curriculumIdx)	{
				final int curriculumID = curriculae.get(curriculumIdx);
				String curriculumName = DatabaseManager.getCurriculum(curriculumID).getName();
				
				Label cLabel = new Label(curriculumName);
				curriculumTab.add(cLabel, 0, curriculumIdx);

				Button removeMajorButton = newRemoveButton();
				curriculumTab.add(removeMajorButton, 1, curriculumIdx);

				removeMajorButton.setOnMouseClicked((me) -> {
					Iterator<Integer> iterator = curriculae.iterator();
					while(iterator.hasNext()) {
						Integer id = iterator.next();
						if(id == curriculumID) {
							iterator.remove();
							break;
						}
					}
					curriculumTab.getChildren().remove(cLabel);
					curriculumTab.getChildren().remove(removeMajorButton);
					
				});
			}
			
			
			actionPane.add(curriculumPane, 0, 0);
			
			//Current Selected Semester Pane
			GridPane currentSelectedSemesterPane = new GridPane();
			currentSelectedSemesterPane.setMaxWidth(actionPane.getPrefWidth()*(2/3.0)*tabWidth);
			currentSelectedSemesterPane.setMinWidth(actionPane.getPrefWidth()*(2/3.0)*tabWidth);
			
			//Header label for Current Selected Semester
			Label headerForCurrentSelectedSemester = newStyledLabel("Current Selected Semester", "fontmed");
			
			//Green Plus Button for Current Selected Semester
			Button addCourseButton = newAddButton();
			
			ScrollPane currentSelectedSemesterScroll = newActionScroll();
			currentSelectedSemesterScroll.setContent(currentSelectedSemesterTab);
//			currentSelectedSemesterScroll.setMinViewportWidth(currentSelectedSemesterTab.getMinWidth());
			currentSelectedSemesterScroll.setMaxWidth(actionPane.getPrefWidth()*tabWidth);
			currentSelectedSemesterScroll.setMinWidth(actionPane.getPrefWidth()*tabWidth);

			currentSelectedSemesterPane.add(headerForCurrentSelectedSemester, 0, 0);
			currentSelectedSemesterPane.add(addCourseButton, 1, 0);
			currentSelectedSemesterPane.add(currentSelectedSemesterScroll, 0, 1);
			
			actionPane.add(currentSelectedSemesterPane, 1, 0);
			actionPane.setHgap(10);
			

			
			
			GridPane addCoursesTab = newActionGrid();
			ScrollPane addCoursesScroll = newActionScroll();
			
			//Search Header Pane
			GridPane searchHeaderPane = new GridPane();
			searchHeaderPane.getColumnConstraints().add(constraints);
			
			//Search Header
			Label headerForAddCourses = newStyledLabel("Add Courses", "fontmed");
			
			//Search Field
			TextField searchField = new TextField();
			searchField.setPromptText("search");
			searchField.getStyleClass().add("windows");
			searchField.setAlignment(Pos.CENTER_RIGHT);
			
			searchField.setOnKeyTyped((ke) -> {
				
				// set request to another thread "searchThread"
				searchThread.setSearchRequest(()-> {
					if(searchField.getText().equals(null) || searchField.getText().equals(""))	{
						// nothing to search, clear previous search results from addCoursesTab
						
						// GUI changes must be on main thread
						final Single<Boolean> waitUntilDoneFlag = new Single<>(false);
						Platform.runLater(() -> {
							addCoursesTab.getChildren().clear();
							waitUntilDoneFlag.single = false;
						});
						waitUntilDone(waitUntilDoneFlag);
						
						return; // no need to do anything further, end lambda functionality here
					}
					
					// Search (Takes forever)
					String searchSemester = Majorizer.getStudentCurrentSemesterString(selectedSemester);
					searchedCourses = DatabaseManager.searchCourse(searchField.getText(), searchSemester);
					Iterator<Course> checkIter = searchedCourses.iterator();
					ArrayList<Integer> studentCurrentCourseload = Majorizer.getStudent().getAcademicPlan().getSelectedCourseIDs().get(searchSemester);
					if(studentCurrentCourseload != null) {
						while(checkIter.hasNext()) {
							Course checkCourse = checkIter.next();
							if(studentCurrentCourseload.contains(checkCourse.getCourseID()))
								checkIter.remove();
						}
					}
					// End of search
					
					// GUI changes must be on main thread

					final Single<Boolean> waitUntilDoneFlag = new Single<>(false);
					Platform.setImplicitExit(false);
					Platform.runLater(() -> {
						addCoursesTab.getChildren().clear();
						for(int searchIndex = 0; searchIndex < searchedCourses.size(); ++searchIndex)	{
							final Course searchedCourse = searchedCourses.get(searchIndex);
							Label searchedLabel = new Label(searchedCourse.getCourseCode() + '\t' + searchedCourse.getCourseName());
							addCoursesTab.add(searchedLabel, 0, searchIndex);
							
							Button addSearchButton = newAddButton();
							addSearchButton.setOnMouseClicked((me) -> {
								Iterator<Course> iterator = searchedCourses.iterator();
								while(iterator.hasNext()) {
									Course currentCourse = iterator.next();
									if(currentCourse.getCourseID() == searchedCourse.getCourseID()) {
										studentCurrentCourseload.add(searchedCourse.getCourseID());
										iterator.remove();
										break;
									}
								}
								addCoursesTab.getChildren().remove(searchedLabel);
								addCoursesTab.getChildren().remove(addSearchButton);
								addCourseToCurrentSemester(searchedCourse);
							});
							addCoursesTab.add(addSearchButton, 1, searchIndex);
						}
						waitUntilDoneFlag.single = true;
					});
					waitUntilDone(waitUntilDoneFlag);
				});
			});
			
			final double tabWidthTwo = 3/4.0;
			addCoursesTab.getStyleClass().add("windows");
			addCoursesTab.setMinSize(screenSize.getWidth()*(2/3.0)*tabWidthTwo, screenSize.getHeight()*(1/5.0));
			addCoursesTab.setMaxSize(screenSize.getWidth()*(2/3.0)*tabWidthTwo, screenSize.getHeight()*(1/5.0));
			addCoursesScroll.setMinSize(screenSize.getWidth()*(2/3.0)*tabWidthTwo, screenSize.getHeight()*(1/5.0));
			addCoursesScroll.setMaxSize(screenSize.getWidth()*(2/3.0)*tabWidthTwo, screenSize.getHeight()*(1/5.0));
			addCoursesScroll.setContent(addCoursesTab);
			
			
			
			
			//Check Button
			Button checkButton = new Button();
			checkButton.setShape(new Circle(2));
			ImageView checkButtonMark = new ImageView(ResourceLoader.getImage("checkmark30.png"));
			checkButton.setGraphic(checkButtonMark);
			checkButtonMark.setFitWidth(40);
			checkButtonMark.setFitHeight(40);
			checkButton.getStyleClass().add("checkbuttontheme");
			checkButton.setAlignment(Pos.BOTTOM_RIGHT);
			
			checkButton.setOnMouseClicked((me) -> {
				Majorizer.scheduleForStudent();
				DatabaseManager.saveStudent(Majorizer.getStudent());
			});
			
			VBox checkButtonBox = new VBox();
			checkButtonBox.getChildren().add(checkButton);
			checkButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
			checkButtonBox.setPadding(new Insets(5));

			
			searchHeaderPane.add(headerForAddCourses, 0, 0);
			searchHeaderPane.add(searchField, 1, 0);
			
			searchPane.setAlignment(Pos.BOTTOM_CENTER);
			searchPane.setVgap(5);
			searchPane.setHgap(20);
			
			searchPane.add(searchHeaderPane, 0, 0);
			searchPane.add(addCoursesScroll, 0, 1);
			searchPane.add(checkButtonBox, 1, 1);
			
			boolean gridLinesOn = false;
			if(gridLinesOn)	{
				searchHeaderPane.setGridLinesVisible(true);
				actionPane.setGridLinesVisible(true);
				searchPane.setGridLinesVisible(true);
				orgPane.setGridLinesVisible(true);
			}

			
			orgPane.add(topPane, 0, 0);
			orgPane.add(schedulePane, 0, 1);
			orgPane.add(actionPane, 0, 2);
			orgPane.add(searchPane, 0, 3);
			orgPane.setVgap(10);
			orgPane.setHgap(5);
			orgPane.setPadding(new Insets(10));			
			
			studentScreen.setCenter(orgPane);
		}	catch( Exception e )	{
			e.printStackTrace();
		}
		
		return studentScreen;
	}
	
	public SplitPane advisorView()	{
		
		BorderPane advisorSide = new BorderPane();
		BorderPane studentSide = new BorderPane();
		SplitPane splitStudentAdvisor = new SplitPane(advisorSide, studentSide);
		
		advisorSide.getStyleClass().add("lightgraytheme");
		
		ColumnConstraints constraints = new ColumnConstraints();
		constraints.setHgrow(Priority.ALWAYS);
		
		GridPane orgPane = new GridPane();
		orgPane.getColumnConstraints().add(constraints);

		Label name = new Label();
		name.setText(getNameAdvisor());
		System.out.println(getNameAdvisor());
		
		name.getStyleClass().add("fonttitle");
		
		//studentList Pane
		GridPane studentListPane = new GridPane();
		studentListPane.setPrefWidth(200);
		
		//Header label for studentList
		Label studentListHeader = newStyledLabel("Students", "fontmed");
		
		//Tab for studentList
		GridPane studentListTab = newActionGrid();
		
		//Scroll for studentList
		ScrollPane studentListScroll = newActionScroll();
		studentListScroll.setContent(studentListTab);
		studentListScroll.setMinViewportWidth(200);
		
		studentListPane.add(studentListHeader, 0, 0);
		studentListPane.add(studentListScroll, 0, 1);
		
		ArrayList<Student> students = Majorizer.getAdvisees();
		for(int studentIdx = 0; studentIdx < students.size(); ++studentIdx)	{
			Student student = students.get(studentIdx); 
			String firstName = student.getFirstName();
			String lastName = student.getLastName();
			
			String fullName = firstName + ' ' + lastName;
			
			Label studentNames = new Label(fullName);
			studentListTab.add(studentNames, 0, studentIdx);
			
			studentNames.setOnMousePressed((me) -> {
				Majorizer.setStudent(student);
				studentSide.setCenter(studentView());
			});
		}
		
		//Requests Pane
		GridPane requestsPane = new GridPane();
		requestsPane.setMaxWidth(200);
		
		//Header label for studentList
		Label requestsHeader = newStyledLabel("List of Requests", "fontmed");
		
		//Tab for requestsList
		GridPane requestsTab = newActionGrid();
		
		//Scroll for studentList
		ScrollPane requestsScroll = newActionScroll();
		requestsScroll.setContent(requestsTab);
		requestsScroll.setMinViewportWidth(200);
		
		requestsPane.add(requestsHeader, 0, 0);
		requestsPane.add(requestsScroll, 0, 1);
		
		ArrayList<Request> requests = Majorizer.getRequests();
		for(int requestIdx = 0; requestIdx < requests.size(); ++requestIdx) {
			Request request = requests.get(requestIdx);
			Student studentName = request.getStudent();
			Curriculum requestMessage = request.getCurriculum();
			
			String firstName = studentName.getFirstName();
			String lastName = studentName.getLastName();
			String fullName = firstName + ' ' + lastName;
			
			String msg = requestMessage.getName();
			
			String fullAddMessage = fullName + " - Add " + msg;
			String fullRemoveMessage = fullName + " - Remove " + msg;
			
			Label studentRequests;
			if(request.isAdding() == true) {
				studentRequests = new Label(fullAddMessage);
			} else {
				studentRequests = new Label(fullRemoveMessage);
			}
			
			requestsTab.add(studentRequests, 0, requestIdx);
		}
		
		orgPane.add(name, 0, 0);
		orgPane.add(studentListPane, 0, 1);
		orgPane.add(requestsPane, 0, 2);
		
		advisorSide.setCenter(orgPane);
		
		return splitStudentAdvisor;
	}

	
	// used for search thread
	private SearchThread searchThread;
	private boolean windowOpen = true; // true when program starts up
	private static final int SEARCH_WAIT_TIME_MILI = 100;
	
	// used for search thread
	private class SearchThread extends Thread {
		private Runnable searchRunnable;
		
		@Override
		public void run() {
			while (windowOpen) {
				if(searchRunnable != null) {
					Runnable toExecute;
					synchronized (this) {
						toExecute = searchRunnable;
						searchRunnable = null;
					}
					
					toExecute.run();
				}
				try {
					Thread.sleep(SEARCH_WAIT_TIME_MILI);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void setSearchRequest(Runnable run) {
			synchronized (this) {
				this.searchRunnable = run;
			}
		}
	}
	
	public static void waitUntilDone(Single<Boolean> flagSingle) {
		while(!flagSingle.single) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {}
		}
	}
	
	
	@Override
	public void start(Stage primaryStage)	{
		try	{		
			updateUI();
			
			searchThread = new SearchThread();
			searchThread.start();   // thread will be running in the background waiting for search requests
			
			primaryStage.setOnCloseRequest((e) -> {
				windowOpen = false; // used to exit out of while loop of search thread
			});
			
			primaryStage.setScene(scene);
			primaryStage.setTitle("Majorizer");
			primaryStage.getIcons().add(ResourceLoader.getImage("favicon.png"));
			primaryStage.show();
			primaryStage.setMaximized(true);
		}	catch(Exception e)	{
			e.printStackTrace();
		}
	}
		
	public static void callLaunch(String[] args) {
		launch(args);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
