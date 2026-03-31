package org.puregxl.site.framework.autoconfigure;

import org.puregxl.site.framework.config.DataBaseConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(DataBaseConfiguration.class)
public class DataBaseAutoConfiguration {
}
