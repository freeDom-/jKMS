Pit Market 2.0 - HowTo add a new translation
============================================

*   Translate the right-hand-side phrases in the messages\_xy.properties file in the folder src/main/resources/lang to a new messages\_yz.properties file in the same folder. Replace yz with the locale of your language as provided by LocaleContextHolder.getLocale() method.
*   Translate the contracts template in the folder sr/main/resources/static/pdf to a new Contracts\_Template\_yz.pdf. Replace yz with the locale of your language as provided by LocaleContextHolder.getLocale() method.