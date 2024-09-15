package pe.edu.upeu.calcfx.control.servicio;

import pe.edu.upeu.calcfx.control.modelo.CalcTO;

import java.util.List;

public interface CalcServiceI {

    public void guardarResultados(CalcTO to); //el ultimo to puede ser cualquier nombre
    public List<CalcTO> obtenerResultados();
    public void actualizarResultado(CalcTO to, int index);
    public void eliminarResultado(int index);



}
