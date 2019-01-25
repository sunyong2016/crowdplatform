package mlab.mcsweb.client.study.sensor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class MagnetometerConfiguration extends Composite {
	@UiField
	Button collapseButton, timeButton, batteryButton, infoButton;
	
	@UiField
	Row headerRow;
	
	@UiField
	HTMLPanel configDetailsPanel, constraintPanel;
	
	private boolean isCollapsed = true;

	private static MagnetometerConfigurationUiBinder uiBinder = GWT.create(MagnetometerConfigurationUiBinder.class);

	interface MagnetometerConfigurationUiBinder extends UiBinder<Widget, MagnetometerConfiguration> {
	}

	public MagnetometerConfiguration() {
		initWidget(uiBinder.createAndBindUi(this));
		
		timeButton.setIcon(IconType.CLOCK_O);
		batteryButton.setIcon(IconType.BATTERY_3);
		infoButton.setIcon(IconType.INFO);

		expand();
	}
	@UiHandler("timeButton")
	void addTimeConstraint(ClickEvent event){
		constraintPanel.add(new TimeConstraintView());
	}
	
	@UiHandler("batteryButton")
	void addEnergyConstraint(ClickEvent event){
		constraintPanel.add(new EnergyConstraintView());
	}
	
	void collapse(){
		timeButton.setVisible(false);
		batteryButton.setVisible(false);
		infoButton.setVisible(false);
		configDetailsPanel.setVisible(false);
		collapseButton.setIcon(IconType.ANGLE_DOWN);
		isCollapsed = true;		
	}
	void expand(){
		timeButton.setVisible(true);
		batteryButton.setVisible(true);
		infoButton.setVisible(true);
		configDetailsPanel.setVisible(true);
		collapseButton.setIcon(IconType.ANGLE_UP);
		isCollapsed = false;
	}
	
	protected void collapseAction(){
		if(isCollapsed){
			expand();
		}else {
			collapse();
		}		
	}
	
	@UiHandler("collapseButton")
	void collapseButtonClicked(ClickEvent event){
		collapseAction();
	}

}