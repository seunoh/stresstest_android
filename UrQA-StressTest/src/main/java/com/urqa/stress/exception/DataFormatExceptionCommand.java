package com.urqa.stress.exception;

import com.urqa.stress.common.Command;

import java.util.zip.DataFormatException;

/**
 * @author seunoh on 2014. 05. 11..
 */
public class DataFormatExceptionCommand extends Command {

    public DataFormatExceptionCommand() {
        setName(DataFormatException.class.getSimpleName());
    }

    @Override
    public void execute() throws Exception {
        throw new DataFormatException(name());
    }
}
