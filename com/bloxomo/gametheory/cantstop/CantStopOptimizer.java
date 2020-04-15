package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.util.*;
import java.io.*;

public class CantStopOptimizer
{
    public static void main(String[] args)
    {
	CantStopState s = new CantStopState(Integer.parseInt(args[0]),
					    Integer.parseInt(args[1]));

	StateValueMap values = s.getMap();

	for (int layer = s.getTotalSpaces(); layer >= 0; layer--)
	    {
		Iterator i = s.iterator(layer);
		while (i.hasNext())
		    {
			CantStopState state = (CantStopState)(i.next());

			if (state.isFinal())
			    {
				values.setValue(state, 0.0);
			    }
			else if (state.hashCode() <= state.mirror().hashCode())
			    {
				double stateValue = state.computeExpectedTurns(values);

				System.out.println(state + " " + state.hashCode());
				System.out.println(state.mirror() + " " + state.mirror().hashCode());
				values.setValue(state, stateValue);
				values.setValue(state.mirror(), stateValue);
			    }
			System.out.println(state + " = " + values.getValue(state));

		    }

	    }

	try
	    {
		DataOutputStream out = new DataOutputStream(new FileOutputStream("cantstop_" + args[0] + "_" + args[1] + ".dat"));
		values.write(out);
		out.close();
	    }
	catch (IOException e)
	    {
		e.printStackTrace(System.err);
	    }
    }
}
