package net.runelite.client.plugins.config.configpanel;

import com.google.common.primitives.Ints;
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
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@Singleton
public class IntSpinnerConfigTypeStrategy implements ConfigTypeStrategy<Integer>
{

	private static final int SPINNER_FIELD_WIDTH = 6;

	@Override
	public boolean appliesToType(Type type)
	{
		return type == int.class;
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.EAST;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public Component createComponent(BiConsumer<Component, Integer> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, Integer existingValue)
	{
		int value = existingValue;

		Range range = cid.getRange();
		int min = 0, max = Integer.MAX_VALUE;
		if (range != null)
		{
			min = range.min();
			max = range.max();
		}

		// Config may previously have been out of range
		value = Ints.constrainToRange(value, min, max);

		SpinnerModel model = new SpinnerNumberModel(value, min, max, 1);
		JSpinner spinner = new JSpinner(model);
		Component editor = spinner.getEditor();
		JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
		spinnerTextField.setColumns(SPINNER_FIELD_WIDTH);
		spinner.addChangeListener(ce -> onNewValue.accept(spinner, (Integer) spinner.getValue()));

		Units units = cid.getUnits();
		if (units != null)
		{
			spinnerTextField.setFormatterFactory(new UnitFormatterFactory(units));
		}

		return spinner;
	}
}
