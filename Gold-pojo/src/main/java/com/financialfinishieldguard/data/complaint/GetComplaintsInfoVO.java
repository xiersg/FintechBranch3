package com.financialfinishieldguard.data.complaint;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GetComplaintsInfoVO {

    private List<ComplaintInfo> complaints;
}
