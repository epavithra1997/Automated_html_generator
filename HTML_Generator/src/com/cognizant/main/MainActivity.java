package com.cognizant.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.cognizant.beans.Layers;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MainActivity {
	static String htmlContent = " ";
	static String cssContent = " ";
	static Stack<String> stack = new Stack<String>();
	static Stack<Float> guideX = new Stack<Float>();
	static Stack<Float> guideY = new Stack<Float>();
	static Stack<String> cssStack = new Stack<String>();
	static String parenttext = " ", childtext = " ", subchildtext = " ";
	static int valX = 0;
	static int valY = 0;

	public static void main(String[] args) throws FileNotFoundException {

		// Reading JSON File using GSON
		Gson gson = new Gson();
		String path = "C:\\Users\\765779\\udhay\\Project\\demo.json";
		BufferedReader br = new BufferedReader(new FileReader(path));
		Map<String, Layers> decoded = gson.fromJson(br, new TypeToken<Map<String, Layers>>() {
		}.getType());

		// Creating HTML and CSS File
		try {
			File htmlFile = new File("C:\\Users\\765779\\udhay\\generatedHTML.html");
			File cssFile = new File("C:\\Users\\765779\\udhay\\generatedCSS.css");

			if (htmlFile.createNewFile() && cssFile.createNewFile()) {

				// Writing HTML and CSS File
				FileWriter htmlWriter = new FileWriter(htmlFile);
				FileWriter cssWriter = new FileWriter(cssFile);
				BufferedWriter htmlBuffer = new BufferedWriter(htmlWriter);
				BufferedWriter cssBuffer = new BufferedWriter(cssWriter);
				htmlBuffer.write(
						"<html>\n<head>\n<link rel = \"stylesheet\" type = \"text/css\" href = \"generatedCSS.css\">\n</head>\n<body>\n");
				for (String layerName : decoded.keySet()) {

					Layers layerValue = decoded.get(layerName);
					List<String> layerParent = layerValue.getParent();
					Map<String, String> cssStyles = layerValue.getCssStyles();
					for (String parent : layerParent) {

						if (parent.equals("ROOT_REF_WINDOW")) {

							guideX.push((float) 0.00);
							guideY.push((float) 0.00);
							parenttext = layerValue.getText();
							cssStack.push(layerValue.getElementName());
							computeStyleGuide(decoded, guideX, guideY, cssStack);

							if (parenttext != null) {
								htmlContent = htmlContent + "\n<span id = \"" + layerValue.getElementName() + " \">"
										+ layerValue.getText() + "</span>\n";

							} else {
								htmlContent = htmlContent + "\n<div id = \"" + layerValue.getElementName() + "\">\n";
								List<String> layerContains = layerValue.getContains();
								childtext = layerValue.getText();
								if (childtext != null) {
									htmlContent = htmlContent + "\n<span id = \"" + layerValue.getElementName() + "\">"
											+ layerValue.getText() + "</span>\n";
								} else if (layerContains.isEmpty()) {
									htmlContent = htmlContent + "\n</div>";
								} else {
									for (String cont : layerContains) {
										stack.push(cont);
									}
									// System.out.println(stack);
									pushFunction(decoded, stack);

									htmlContent = htmlContent + "</div>";
								}
							}
						}
					}

				}
				for (String layerName : decoded.keySet()) {

					if (!layerName.equals("ROOT_REF_WINDOW")) {

						Layers layerValue = decoded.get(layerName);
						List<String> layerParent = layerValue.getParent();
						// System.out.println(layerName);
						Map<String, String> cssStyles = layerValue.getCssStyles();
						parenttext = layerValue.getText();

						if (parenttext != null) {
							// System.out.println(""+ parenttext);
							cssContent = cssContent + "#" + layerValue.getElementName() + "{\nposition:absolute;\n";
						} else {

							cssContent = cssContent + "#" + layerValue.getElementName()
									+ "{\nposition:absolute;\nborder:solid 1px black;\n";
						}
						cssContent = cssContent + "height:" + layerValue.getHeight() + ";\n";
						cssContent = cssContent + "width:" + layerValue.getWidth() + ";\n";
						cssContent = cssContent + "left:" + layerValue.getStyleGuideX() + ";\n";
						cssContent = cssContent + "top:" + layerValue.getStyleGuideY() + ";\n";

						for (String map : cssStyles.keySet()) {
							// System.out.println(map);
							cssContent = cssContent + map + ":" + cssStyles.get(map) + ";\n";
						}
						cssContent = cssContent + "}\n";
						// htmlBuffer.write(htmlContent);

					} else {
						Layers layerValue = decoded.get(layerName);
						List<String> layerParent = layerValue.getParent();

						Map<String, String> cssStyles = layerValue.getCssStyles();
						parenttext = layerValue.getText();
						if (parenttext != null) {
							// System.out.println(""+ parenttext);
							cssContent = cssContent + "#" + layerValue.getElementName() + "{\nposition:absolute;\n";
						}
					}

					// File Close

				}
				cssBuffer.write(cssContent);
				htmlBuffer.write(htmlContent);
				htmlBuffer.write("\n</body>\n</html>");
				htmlBuffer.close();
				cssBuffer.close();
			} else {
				System.out.println("file already exists");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void computeStyleGuide(Map<String, Layers> decoded, Stack<Float> guideX2, Stack<Float> guideY2,
			Stack<String> cssStack2) {
		if (!cssStack2.peek().equals("ROOT_REF_WINDOW")) {
			while (!cssStack2.isEmpty()) {
				if (cssStack2.peek().equals("-1")) {
					guideX2.pop();
					guideY2.pop();
					cssStack2.pop();
				} else {
					Layers layer = decoded.get(cssStack2.peek());
					List<String> layerContains = layer.getContains();
					String s = layer.getStyleGuideX();
					String newX = s.substring(0, s.length() - 2);
					String lasting = s.substring(s.length() - 2, s.length());
					Float layX = Float.parseFloat(newX);
					String s1 = layer.getStyleGuideY();
					String lasting1 = s1.substring(s1.length() - 2, s1.length());
					String newY = s1.substring(0, s1.length() - 2);
					Float layY = Float.parseFloat(newY);
					if (layerContains.isEmpty()) {

						float resX = Math.abs(layX - guideX2.peek());

						float resY = Math.abs(layY - guideY2.peek());
						layer.setStyleGuideX(String.valueOf(resX) + lasting);
						layer.setStyleGuideY(String.valueOf(resY) + lasting1);
						cssStack2.pop();

					} else {
						// cssStack2.push(layX - guideX2.peek());
						float setX = Math.abs(layX - guideX2.peek());
						layer.setStyleGuideY(String.valueOf(setX) + lasting);
						float setY = Math.abs(layY - guideY2.peek());
						layer.setStyleGuideY(String.valueOf(setY) + lasting1);
						cssStack2.pop();
						cssStack2.push("-1");
						float tempX = layX;
						guideX2.push(tempX);
						float tempY = layY;
						guideY2.push(tempY);

						for (String tmpString : layerContains) {
							cssStack2.push(tmpString);

						}

					}
				}
			}
		}

	}

	private static void pushFunction(Map<String, Layers> decoded, Stack<String> stack2) {
		// int val x = lay.stx, int valy =

		while (!stack2.empty()) {
			if (stack2.peek().equals("-1")) {
				htmlContent = htmlContent + "\n</div>";
				stack2.pop();

			} else {
				Layers subLayer = decoded.get(stack2.peek());
				List<String> subLayerContains = subLayer.getContains();
				subchildtext = subLayer.getText();

				if (subchildtext != null) {

					htmlContent = htmlContent + "\n<span id = \"" + subLayer.getElementName() + "\">"
							+ subLayer.getText() + "</span>\n";

				} else {
					htmlContent = htmlContent + "\n<div id = \"" + subLayer.getElementName() + "\"></div>\n";
				}

				if (subLayerContains.isEmpty()) {

					stack2.pop();
				}
				else
				{
					htmlContent = htmlContent + "\n<div id = \"" + subLayer.getElementName() + "\">";
					stack2.pop();
					stack2.push("-1");
					for (String contain : subLayerContains) {
						stack2.push(contain);
					}
				}
			}
		}

	}

}
