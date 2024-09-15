package pe.edu.upeu.calcfx.control;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.upeu.calcfx.control.modelo.CalcTO;
import pe.edu.upeu.calcfx.control.servicio.CalcServiceI;


import java.util.List;

@Component
public class CalcControl {

    @Autowired
    CalcServiceI serviceI;

    @FXML
    TextField txtResultado;

    @FXML
    TableView tableView;

    @FXML
    TableColumn <CalcTO, String> cval1,cval2,cResul;

    @FXML
    TableColumn<CalcTO, Character> cOper;

    @FXML
    TableColumn<CalcTO, Void> cOpc;

    private ObservableList<CalcTO> calcTOList;
    private int indexEdit=-1;


    @FXML
    public void accionButton(ActionEvent event) {
        Button button = (Button) event.getSource();
        switch (button.getId()) {
            case "btn7", "btn8", "btn9", "btn6", "btn5", "btn4", "btn3", "btn2", "btn1", "btn0"
                    -> escribirNumeros(button.getText());
            case "btnSum", "btnMul", "btnRest", "btnDiv", "btnPotencia"
                    -> operador(button.getText());
            case "btnRaiz"-> calcularRaiz();
            case "btnUnoSobreX"-> calcularInverso();
            case "btnBin" -> convertirBinario();
            case "btnPorcentaje" -> calcularPorcentaje();
            case "btnPI" -> escribirPi();
            case "btnIgual" -> calcularResultado();
            case "btnBorrarTodo" -> txtResultado.clear();
            case "btnBorrar" -> borrarUltimoCaracter();
        }
    }

    public void borrarUltimoCaracter() {
        String textoActual = txtResultado.getText();
        if (textoActual != null && textoActual.length() > 0) {
            txtResultado.setText(textoActual.substring(0, textoActual.length() - 1));
        }
    }

    public void escribirNumeros(String valor){
        txtResultado.appendText(valor);
    }

    public void operador(String valor){
        txtResultado.appendText(" "+valor+" ");}

    public void calcularResultado() {
        try {
            String input = txtResultado.getText().trim();
            String[] valores = input.split("\\s+"); // Dividimos por uno o más espacios

            if (valores.length == 1) { // Si solo hay un número, lo mostramos
                txtResultado.setText(valores[0]);
                return;
            }
            double val1 = Double.parseDouble(valores[0]);

            if (valores.length == 2) {
                String operador = valores[1];
                switch (operador) {
                    case "√" -> calcularRaiz();
                    case "1/x" -> calcularInverso();
                    case "Bin" -> convertirBinario();
                    case "%" -> calcularPorcentaje();
                    default -> txtResultado.setText("Error");
                }
            } else if (valores.length == 3) {
                double val2 = Double.parseDouble(valores[2]);
                switch (valores[1]) {
                    case "+" -> txtResultado.setText(String.valueOf(val1 + val2));
                    case "-" -> txtResultado.setText(String.valueOf(val1 - val2));
                    case "/" -> txtResultado.setText(String.valueOf(val1 / val2));
                    case "*" -> txtResultado.setText(String.valueOf(val1 * val2));
                    case "^" -> txtResultado.setText(String.valueOf(Math.pow(val1, val2)));
                    default -> txtResultado.setText("Error");
                }

                CalcTO to=new CalcTO();
                to.setNum1(String.valueOf(val1));
                to.setNum2(String.valueOf(val2));
                to.setOperador(valores[1].charAt(0));
                to.setResultaso(String.valueOf(txtResultado.getText()));
                if(indexEdit!=-1){
                    serviceI.actualizarResultado(to, indexEdit);
                }else{
                    serviceI.guardarResultados(to);
                }
                indexEdit=-1;
                listaOper();

            } else {
                txtResultado.setText("Formato inválido");
            }
        } catch (Exception e) {
            txtResultado.setText("Error en la operación");
        }
    }

    // Acción para editar una operación
    private void editOperCalc(CalcTO cal, int index) {
        System.out.println("Editing: " + cal.getNum1() + " Index:"+index);
        txtResultado.setText(cal.getNum1()+" "+cal.getOperador()+" "+cal.getNum2());
        indexEdit=index;
    }

    private void deleteOperCalc(CalcTO cal, int index) {
        System.out.println("Deleting: " + cal.getNum2());
        serviceI.eliminarResultado(index);
        listaOper();
        //tableView.getItems().remove(cal);  // Elimina la operación del TableView
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<CalcTO, Void>, TableCell<CalcTO, Void>>
                cellFactory = param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            {
                editButton.getStyleClass().setAll("btn", "btn-success");
                editButton.setOnAction(event -> {
                    CalcTO cal = getTableView().getItems().get(getIndex());
                    editOperCalc(cal, getIndex());
                });
                deleteButton.getStyleClass().setAll("btn", "btn-danger");  //estilo rojo
                deleteButton.setOnAction(event -> {
                    CalcTO cal = getTableView().getItems().get(getIndex());
                    deleteOperCalc(cal, getIndex());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(editButton, deleteButton);
                    buttons.setSpacing(10); //espaciado de 10 entre ambos botones
                    setGraphic(buttons);
                }
            }
        };
        cOpc.setCellFactory(cellFactory);
    }

    public void escribirPi() {
        try {
            double piValue = Math.PI;
            txtResultado.setText(String.valueOf(piValue));

            // Crear una nueva entrada de operación para Pi
            CalcTO to = new CalcTO();
            to.setNum1("π"); // Muestra el símbolo Pi en lugar del valor numérico
            to.setNum2("");  // No hay segundo número
            to.setOperador(' '); // No hay un operador para Pi, lo dejamos vacío
            to.setResultaso(String.valueOf(piValue));

            // Guardar la operación en el servicio y actualizar la tabla
            serviceI.guardarResultados(to);
            listaOper(); // Actualizar la lista de operaciones en la tabla
        } catch (Exception e) {
            txtResultado.setText("Error");
        }
    }
    public void calcularRaiz() {
        try {
            double val = Double.parseDouble(txtResultado.getText());
            double resultado = Math.sqrt(val);
            txtResultado.setText(String.valueOf(resultado));

            // Crear una nueva entrada de operación
            CalcTO to = new CalcTO();
            to.setNum1(String.valueOf(val));
            to.setNum2(""); // La raíz solo tiene un operando, así que dejamos el segundo vacío
            to.setOperador('√');
            to.setResultaso(String.valueOf(resultado));

            // Guardar la operación en el servicio y actualizar la tabla
            serviceI.guardarResultados(to);
            listaOper(); // Actualizar la lista de operaciones en la tabla
        } catch (NumberFormatException e) {
            txtResultado.setText("Error");
        }
    }
    public void calcularPorcentaje() {
        try {
            double val = Double.parseDouble(txtResultado.getText());
            double resultado = val / 100;
            txtResultado.setText(String.valueOf(resultado));

            CalcTO to = new CalcTO();
            to.setNum1(String.valueOf(val));
            to.setNum2("");
            to.setOperador('%');
            to.setResultaso(String.valueOf(resultado));

            serviceI.guardarResultados(to);
            listaOper(); // Actualizar la lista de operaciones en la tabla
        } catch (NumberFormatException e) {
            txtResultado.setText("Error");
        }
    }
    public void calcularInverso() {
        try {
            double val = Double.parseDouble(txtResultado.getText());
            double resultado = 1 / val;
            txtResultado.setText(String.valueOf(resultado));

            CalcTO to = new CalcTO();
            to.setNum1(String.valueOf(val));
            to.setNum2("");
            to.setOperador('1'); // Podrías usar un carácter especial para esta operación
            to.setResultaso(String.valueOf(resultado));

            serviceI.guardarResultados(to);
            listaOper(); // Actualizar la lista de operaciones en la tabla
        } catch (NumberFormatException e) {
            txtResultado.setText("Error");
        }
    }

    public void convertirBinario() {
        try {
            int val = Integer.parseInt(txtResultado.getText());
            String resultado = Integer.toBinaryString(val);
            txtResultado.setText(resultado);

            CalcTO to = new CalcTO();
            to.setNum1(String.valueOf(val));
            to.setNum2("");
            to.setOperador('B'); // Podrías usar un carácter especial para binario
            to.setResultaso(resultado);

            serviceI.guardarResultados(to);
            listaOper(); // Actualizar la lista de operaciones en la tabla
        } catch (NumberFormatException e) {
            txtResultado.setText("Error");
        }
    }

    public void listaOper(){
        List<CalcTO> lista=serviceI.obtenerResultados();
        for (CalcTO to:lista) {
            System.out.println(to.toString());
        }
        tableView.getColumnResizePolicy();
        // Vincular columnas con propiedades de CalcTO
        cval1.setCellValueFactory(new PropertyValueFactory<CalcTO,
                String>("num1"));

        cval1.setCellFactory(TextFieldTableCell.<CalcTO>forTableColumn());

        cval2.setCellValueFactory(new PropertyValueFactory<CalcTO,
                String>("num2"));

        cval2.setCellFactory(TextFieldTableCell.<CalcTO>forTableColumn());

       cOper.setCellValueFactory(new
                PropertyValueFactory<>("operador"));
       cOper.setCellFactory(ComboBoxTableCell.<CalcTO,
                Character>forTableColumn('+', '-', '/', '*'));
        cResul.setCellValueFactory(new PropertyValueFactory<CalcTO,
                String>("resultaso"));

       cResul.setCellFactory(TextFieldTableCell.<CalcTO>forTableColumn());
        // Agregar botones de eliminar y modificar
        addActionButtonsToTable();
        calcTOList = FXCollections.observableArrayList(serviceI.obtenerResultados());
        // Asignar los datos al TableView
        AnchorPane.setLeftAnchor(tableView, 0.0);
        AnchorPane.setRightAnchor(tableView, 0.0);

        cOper.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25)); // 25% del ancho total

        cResul.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25)); // 25% del ancho total

        cOpc.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        tableView.setItems(calcTOList);
    }

}
