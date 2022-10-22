package net.runelite.client.plugins.config.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import javax.inject.Singleton;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;

@Singleton
public class DoubleSpinnerConfigTypeStrategy implements ConfigTypeStrategy<Double>
{

	private static final int SPINNER_FIELD_WIDTH = 6;

	@Override
	public boolean appliesToType(Type type)
	{
		return type == double.class;
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.EAST;
	}

	@Override
	public Component createComponent(BiConsumer<Component, Double> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, Double existingValue)
	{
		double value = existingValue;

		SpinnerModel model = new SpinnerNumberModel(value, 0, Double.MAX_VALUE, 0.1);
		JSpinner spinner = new JSpinner(model);
		Component editor = spinner.getEditor();
		JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
		spinnerTextField.setColumns(SPINNER_FIELD_WIDTH);
		spinner.addChangeListener(ce -> onNewValue.accept(spinner, (Double) spinner.getValue()));
		return spinner;
	}
}
