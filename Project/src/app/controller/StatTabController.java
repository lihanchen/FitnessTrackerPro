package app.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import org.w3c.dom.css.Counter;

import app.model.ActivityPlan;
import app.model.DayPlan;
import app.model.RealActivityPlan;
import app.model.RealDayPlan;
import app.model.Unit;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;

public class StatTabController {
	@FXML
    private AreaChart<String, Number> areaChart;
	@FXML
    private CategoryAxis xAxis;
	@FXML
	private NumberAxis yAxis;
    @FXML
    private ComboBox<String> planNameBox;
    @FXML
    private ComboBox<String> timeScaleBox;
    
    private ObservableList<RealDayPlan> chartPlans;
    
    private ObservableList<String> dayNames;
    
    private XYChart.Series<String, Number> seriesRealData;
    
    private XYChart.Series<String, Number> seriesPlannedData;
    
    private String weekDayName[];
    
    private String monthDayName[];
    
    
    public StatTabController() {
    	
    }
    
    @FXML
    private void initialize() {
    	weekDayName = new DateFormatSymbols().getWeekdays(); //add day name to a string
    	monthDayName = getMonthDayName();
    	//PlansCreator weekPlanCreator = new PlansCreator();
    	//chartPlans = weekPlanCreator.create(); //get the last 7-day activity plan as weekPlan
    	start(7);
    	//xAxis = new CategoryAxis();
    	
    	
    	timeScaleBox.getItems().addAll(
        		"Week",
        		"Month"
        	);
        	timeScaleBox.setValue("Week");
        	timeScaleBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
        		public void changed (ObservableValue ov, Number value, Number newValue) {
        			if(timeScaleBox.getValue() == "Week")
        				start(7);
        			else {
						start(30);
					}
        		}
    		});
    	
    }
    
    
    private String[] getMonthDayName() {
    	monthDayName = new String[30];
    	LocalDate today = LocalDate.now();
    	for(int i = 30; i>0; i--) {
    		monthDayName[30 - i] = today.minusDays(i-1).toString().substring(4,10);
    	}
    	
    	return monthDayName;
    }
    
    
    private void start(int length) {
    	
    	dayNames = FXCollections.observableArrayList();
    	chartPlans = getChartData(loadChartData(), length);
    	
    		//for(RealDayPlan dayPlan : chartPlans) {
    			//dayNames.add(weekDayName[dayPlan.getDate().getDay() + 1].substring(0, 3)); //add to our ObservableList of dayNames
    		//}
    		for(RealDayPlan dayPlan : chartPlans) {
    			dayNames.add(dayPlan.getDate().toString().substring(4,10));
    			System.out.println(dayPlan.getDate().toString().substring(4,10));
    		}
    		
    	addPlanNames();
    	xAxis = new CategoryAxis(dayNames);
    	xAxis.setCategories(dayNames);
    	//xAxis.setLabel("Week");
    	//yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
    	seriesRealData = new XYChart.Series<String, Number>();
    	seriesPlannedData = new XYChart.Series<String, Number>();  	
    	areaChart.setTitle("Statistics");
    }
    
    

	private void addPlanNames() {
    	Collection<RealActivityPlan> tmpActivityPlans = FXCollections.observableArrayList();
    	ObservableList<String> dayPlanNames = FXCollections.observableArrayList();
    	for(RealDayPlan dayPlan : chartPlans) {
    		tmpActivityPlans = dayPlan.getDayPlan().values();
    		for(RealActivityPlan realActivityPlan : tmpActivityPlans) {
    			if(!dayPlanNames.contains(realActivityPlan.getActivityPlan().getActivity().getActvityName())) { //check if the plan is already in the list
    				dayPlanNames.add(realActivityPlan.getActivityPlan().getActivity().getActvityName());
    			}
    		}
    	}
    	
    	Collections.sort(dayPlanNames);
    	planNameBox.setItems(dayPlanNames);
    	planNameBox.setValue(dayPlanNames.get(0));
    	System.out.println("Item in the planNameBox :"+planNameBox.getValue());
    	setActivityData();
    	planNameBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
    		public void changed (ObservableValue ov, Number value, Number newValue) {
    			setActivityData();
    		}
		});
    	planNameBox.setTooltip(new Tooltip("Select activity name please"));
    }
    
	
	
    private void setActivityData() {
    	seriesRealData = new XYChart.Series<String, Number>();
    	seriesPlannedData = new XYChart.Series<String, Number>();
    	seriesRealData.setName("RealData");
    	seriesPlannedData.setName("PlannedData");
    	
    	int maxRange = 0;
    	
    	for(RealDayPlan dayPlan : chartPlans) {
    		System.out.println("Now it's finding :"+planNameBox.getValue());
    		
    		if(dayPlan.getDayPlan().get(planNameBox.getValue()) != null) { 
    			seriesRealData.getData().add(new XYChart.Data<String, Number>(dayPlan.getDate().toString().substring(4,10), 
    					dayPlan.getDayPlan().get(planNameBox.getValue()).getRealCount()));	//add data of selected activity to series
    					if(dayPlan.getDayPlan().get(planNameBox.getValue()).getRealCount() > maxRange) {
    						maxRange = dayPlan.getDayPlan().get(planNameBox.getValue()).getRealCount(); //update max-Range of the yAxis
    					}
    			seriesPlannedData.getData().add(new XYChart.Data<String, Number>(dayPlan.getDate().toString().substring(4,10),
    					dayPlan.getDayPlan().get(planNameBox.getValue()).getActivityPlan().getPlannedCount()));// add data of planned activity to series
    					if(dayPlan.getDayPlan().get(planNameBox.getValue()).getActivityPlan().getPlannedCount() > maxRange) {
    						maxRange = dayPlan.getDayPlan().get(planNameBox.getValue()).getActivityPlan().getPlannedCount();
    					}
    					System.out.println("The real count of "+planNameBox.getValue()+" at "+dayPlan.getDate().toString()+" is "+dayPlan.getDayPlan().get(planNameBox.getValue()).getRealCount());
    					System.out.println("The planned count is "+dayPlan.getDayPlan().get(planNameBox.getValue()).getActivityPlan().getPlannedCount());
    		}
    		else {
    			seriesRealData.getData().add(new XYChart.Data<String, Number>(dayPlan.getDate().toString().substring(4,10), 0));
    			seriesPlannedData.getData().add(new XYChart.Data<String, Number>(dayPlan.getDate().toString().substring(4,10), 0));
    		}
    	}
    	yAxis.setUpperBound(maxRange+5-maxRange%5);
    	areaChart.getData().clear();
    	//areaChart.setVerticalGridLinesVisible(true);
    	areaChart.getData().addAll(seriesPlannedData,seriesRealData);
    }
    
    
    
    private ObservableList<RealDayPlan> getChartData(ObservableList<RealDayPlan> plans, int length) {
    	if(plans.size() < length) {
    		return plans;
    	}
    	else {
    		ObservableList<RealDayPlan> graphPlans = FXCollections.observableArrayList();
    		int index = plans.size() - length;
    		while(index < plans.size()) {
    			graphPlans.add(plans.get(index));
    			index++;
    		}
    		return graphPlans;
    	}
	}
    
    
    
    private ObservableList<RealDayPlan> loadChartData() {
    	
    	ObservableList<RealDayPlan> plans = FXCollections.observableArrayList();
    	Date date = new Date();
    	
    	try{
			File file = new File("./History.Fitness");
			Scanner scan = new Scanner(file);
			String input;
			input = scan.nextLine();
			
			//int counter = 0;
			while (input.equals("<DayHistory>")) {	
				input = scan.nextLine();
				if (input.equals("<Date>")){
					input = scan.nextLine(); //Date
					DateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
					try {
						date = dayFormat.parse(input);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					input = scan.nextLine();
					input = scan.nextLine();//real activity information
					
					ObservableMap<String, RealActivityPlan> map = FXCollections.observableHashMap();
					MapProperty<String, RealActivityPlan> Map;
					
					while(!input.equals("</DayHistory>")) { //
						String info[] = input.split("\\|");

						if(info[1].equals("MINUTE")){
							map.put(info[0], new RealActivityPlan(new ActivityPlan(info[0], Unit.MINUTE, Integer.parseInt(info[2])), Integer.parseInt(info[3])));
						}
						else {
							map.put(info[0], new RealActivityPlan(new ActivityPlan(info[0], Unit.TIMES, Integer.parseInt(info[2])), Integer.parseInt(info[3])));
						}
						input = scan.nextLine();
					}
					//end adding real activities for one day
					Map = new SimpleMapProperty<>(map);
					plans.add(new RealDayPlan(Map, date));
					//counter++;
					
				}
				else {
					scan.close();
					System.out.println("Missing date!");
					//return plans;
				}
				if(scan.hasNextLine()){
					input = scan.nextLine();
				}
			}
			scan.close();
			//return plans;
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
    	
    	try{
    		File file = new File("./TodayPlan.Fitness");
    		Scanner scan = new Scanner(file);
    		String input;
    		
    		input = scan.nextLine();
    		input = scan.nextLine();
    		input = scan.nextLine();
    		input = scan.nextLine();
    		
    		ObservableMap<String, RealActivityPlan> map = FXCollections.observableHashMap();
			MapProperty<String, RealActivityPlan> Map;
			
			while(!input.isEmpty()) { //
				String info[] = input.split("\\|");
				
				if(info[1].equals("MINUTE")){
					map.put(info[0], new RealActivityPlan(new ActivityPlan(info[0], Unit.MINUTE, Integer.parseInt(info[2])), Integer.parseInt(info[3])));
				}
				else {
					map.put(info[0], new RealActivityPlan(new ActivityPlan(info[0], Unit.TIMES, Integer.parseInt(info[2])), Integer.parseInt(info[3])));
				}
				if(scan.hasNextLine()){
					input = scan.nextLine();
				}
				else {
					break;
				}
					
			}
			//end adding real activities for today
			Map = new SimpleMapProperty<>(map);
			plans.add(new RealDayPlan(Map, new Date()));
    		scan.close();
    		return plans;
    	}
    	catch(FileNotFoundException e){
    		e.printStackTrace();
    	}
		return plans;
    }
	
}
