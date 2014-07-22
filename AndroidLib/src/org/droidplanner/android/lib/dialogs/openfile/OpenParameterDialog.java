package org.droidplanner.android.lib.dialogs.openfile;

import java.util.List;

import org.droidplanner.android.lib.utils.file.IO.ParameterReader;
import org.droidplanner.core.parameters.Parameter;

public abstract class OpenParameterDialog extends OpenFileDialog {
	public abstract void parameterFileLoaded(List<Parameter> parameters);

	@Override
	protected FileReader createReader() {
		return new ParameterReader();
	}

	@Override
	protected void onDataLoaded(FileReader reader) {
		parameterFileLoaded(((ParameterReader) reader).getParameters());
	}
}