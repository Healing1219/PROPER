package gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import backend.CodeTreeBuilder;
import backend.Intepretor;
import backend.Util;
import backend.Util.PathCom;
import backend.Util.Var;
import frontend.GrammarStateManager;
import frontend.ProductionManager;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

	private Stage primaryStage;
	private Pane rootLayout;
	@FXML
	private MenuItem a;

	@Override
	public void start(Stage primaryStage) {
		ProductionManager productionManager = ProductionManager.getProductionManager();
		productionManager.initProductions();
		productionManager.runFirstSetAlgorithm();
		GrammarStateManager stateManager = GrammarStateManager.getGrammarManager();
		stateManager.buildTransitionStateMachine();
		try {
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("PROPER");
			// primaryStage.getIcons().add(new
			// Image("file:src/gui/image/Quantum-setup-icon.png"));

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("Main.fxml"));
			rootLayout = loader.load();
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			MainController controller = loader.getController();
			controller.setMain(this);
			primaryStage.show();
		} catch (Exception e) {
			System.out.println(e.getMessage() + "...");
		}
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}

	/*public void showPath(String name, String id, int n, boolean flag) throws IOException {
		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(Main.class.getResource(name));
		TabPane page = loader.load();
		Tab[] tabs = new Tab[n];
		TextArea[] t = new TextArea[n];
		int i = 0;

		for (Map.Entry<ArrayList<Integer>, PathCom> entry : Util.pathMap.entrySet()) {
			tabs[i] = new Tab("path-" + (i + 1));
			t[i] = new TextArea();
			String s = "";
			ArrayList<Var> varList = entry.getValue().getVarList();
			s += "包含" + varList.size() + "个变量: \n";
			s = Util.readVariable(varList, s);
			s += "\n";
			ArrayList<ArrayList<Double>> constrainList = entry.getValue().getConstrainList();
			s += "包含" + constrainList.size() + "个不等式约束: \n";
			s = Util.readConstraint(constrainList, s);
			if (flag) {
				ArrayList<ArrayList<Double>> constrainProList = entry.getValue().getConstrainProList();
				s = Util.readConstraint(constrainProList, s);
			}
			t[i].setText(s);
			tabs[i].setContent(t[i]);
			i++;
		}

		page.getTabs().addAll(tabs);

		Stage stg = new Stage();
		stg.setTitle(id);
		stg.initModality(Modality.WINDOW_MODAL);
		stg.initOwner(primaryStage);
		Scene scene = new Scene(page);
		stg.setScene(scene);
		stg.showAndWait();
	}*/

}
