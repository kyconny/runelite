package net.runelite.client.plugins.config.configpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ColorUtil;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ColorPickerConfigTypeStrategy implements ConfigTypeStrategy<Color>
{

	private final ColorPickerManager colorPickerManager;

	@Override
	public boolean appliesToType(Type type)
	{
		return type == Color.class;
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.EAST;
	}

	@Override
	public Component createComponent(BiConsumer<Component, Color> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, Color existing)
	{
		ColorJButton colorPickerBtn;

		boolean alphaHidden = cid.getAlpha() == null;

		if (existing == null)
		{
			colorPickerBtn = new ColorJButton("Pick a color", Color.BLACK);
		}
		else
		{
			String colorHex = "#" + (alphaHidden ? ColorUtil.colorToHexCode(existing) : ColorUtil.colorToAlphaHexCode(existing)).toUpperCase();
			colorPickerBtn = new ColorJButton(colorHex, existing);
		}

		colorPickerBtn.setFocusable(false);
		colorPickerBtn.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				RuneliteColorPicker colorPicker = colorPickerManager.create(
					SwingUtilities.windowForComponent(parent),
					colorPickerBtn.getColor(),
					cid.getItem().name(),
					alphaHidden);
				colorPicker.setLocation(parent.getLocationOnScreen());
				colorPicker.setOnColorChange(c ->
				{
					colorPickerBtn.setColor(c);
					colorPickerBtn.setText("#" + (alphaHidden ? ColorUtil.colorToHexCode(c) : ColorUtil.colorToAlphaHexCode(c)).toUpperCase());
				});
				colorPicker.setOnClose(c -> onNewValue.accept(colorPicker, colorPicker.getSelectedColor()));
				colorPicker.setVisible(true);
			}
		});

		return colorPickerBtn;
	}
}
