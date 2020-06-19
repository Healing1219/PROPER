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

}
