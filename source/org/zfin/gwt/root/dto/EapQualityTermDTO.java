package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.Window;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class EapQualityTermDTO implements Serializable, Comparable<EapQualityTermDTO> {

    private TermDTO term;
    private String tag;
    private String nickName;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public TermDTO getTerm() {
        return term;
    }

    public void setTerm(TermDTO term) {
        this.term = term;
    }

    public String getNickName() {
        if (nickName != null)
            return nickName;

        String nn = nicknameMap.get(term.getOboID() + "," + tag);
        if (nn == null)
            return "No nick name found";
        return nn;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }


    public static Map<String, String> nicknameMap = new LinkedHashMap<>(12);

    static {
        nicknameMap.put("PATO:0000462,abnormal", "absent phenotypic");
        nicknameMap.put("PATO:0000628,abnormal", "mislocalized");
        nicknameMap.put("PATO:0000140,ameliorated", "position ok");
        nicknameMap.put("PATO:0001672,abnormal", "decreased distribution");
        nicknameMap.put("PATO:0001671,abnormal", "increased distribution");
        nicknameMap.put("PATO:0000060,abnormal", "spatial pattern abnormal");
        nicknameMap.put("PATO:0000060,ameliorated", "spatial pattern ok");
        nicknameMap.put("PATO:0001997,abnormal", "decreased amount");
        nicknameMap.put("PATO:0000470,abnormal", "increased amount");
        nicknameMap.put("PATO:0000070,ameliorated", "amount ok");
    }


    @Override
    public int compareTo(EapQualityTermDTO o) {
        List<String> list = new ArrayList<>(nicknameMap.values());
        return list.indexOf(o.getNickName()) - list.indexOf(nickName);
    }
}