package net.runelite.client.plugins.config.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import javax.inject.Singleton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;

@Singleton
public class DimensionConfigTypeStrategy implements ConfigTypeStrategy<Dimension>
{
	@Override
	public boolean appliesToType(Type type)
	{
		return type == Dimension.class;
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.EAST;
	}

	@Override
	public Component createComponent(BiConsumer<Component, Dimension> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, Dimension existingValue)
	{
		JPanel dimensionPanel = new JPanel();
		dimensionPanel.setLayout(new BorderLayout());

		int width = existingValue.width;
		int height = existingValue.height;

		SpinnerModel widthModel = new SpinnerNumberModel(width, 0, Integer.MAX_VALUE, 1);
		JSpinner widthSpinner = new JSpinner(widthModel);
		Component widthEditor = widthSpinner.getEditor();
		JFormattedTextField widthSpinnerTextField = ((JSpinner.DefaultEditor) widthEditor).getTextField();
		widthSpinnerTextField.setColumns(4);

		SpinnerModel heightModel = new SpinnerNumberModel(height, 0, Integer.MAX_VALUE, 1);
		JSpinner heightSpinner = new JSpinner(heightModel);
		Component heightEditor = heightSpinner.getEditor();
		JFormattedTextField heightSpinnerTextField = ((JSpinner.DefaultEditor) heightEditor).getTextField();
		heightSpinnerTextField.setColumns(4);

		ChangeListener listener = e -> onNewValue.accept(dimensionPanel, new Dimension((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue()));

		widthSpinner.addChangeListener(listener);
		heightSpinner.addChangeListener(listener);

		dimensionPanel.add(widthSpinner, BorderLayout.WEST);
		dimensionPanel.add(new JLabel(" x "), BorderLayout.CENTER);
		dimensionPanel.add(heightSpinner, BorderLayout.EAST);

		return dimensionPanel;
	}
}
