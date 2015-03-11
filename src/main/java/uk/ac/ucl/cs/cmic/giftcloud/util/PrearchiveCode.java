/**
 * PrearchiveCode
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 12/19/11 by rherri01
 */
package uk.ac.ucl.cs.cmic.giftcloud.util;

import java.util.HashMap;
import java.util.Map;

public enum PrearchiveCode {
    Manual (0),
    AutoArchive (4),
    AutoArchiveOverwrite (5);

    private final int _code;
    private static final Map<Integer, PrearchiveCode> _codes = new HashMap<Integer, PrearchiveCode>();

    PrearchiveCode(int code) {
        _code = code;
    }

    public int getCode() {
        return _code;
    }

    public static PrearchiveCode code(String code) {
        return code(Integer.parseInt(code));
    }

    public static PrearchiveCode code(int code) {
        if (_codes.isEmpty()) {
            synchronized (PrearchiveCode.class) {
                for (PrearchiveCode prearchiveCode : values()) {
                    _codes.put(prearchiveCode.getCode(), prearchiveCode);
                }
            }
        }
        return _codes.get(code);
    }
    
    @Override
    public String toString() {
        return this.name();
    }
}
