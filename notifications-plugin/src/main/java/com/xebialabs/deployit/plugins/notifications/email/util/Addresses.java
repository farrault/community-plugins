/*
 * @(#)Addresses.java     22 Sep 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.deployit.plugins.notifications.email.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.xebialabs.deployit.plugins.notifications.email.util.Addresses.NameAndAddress.toNameAndAddress;
import static org.codemonkey.simplejavamail.EmailValidationUtil.isValid;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;

import org.codemonkey.simplejavamail.Email;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Addresses {
    
    public static List<Recipient> toRecipients(List<String> addresses, 
            final RecipientType type) {
        return Lists.transform(addresses, new Function<String, Recipient>() {
            @Override
            public Recipient apply(String input) {
                return new Recipient(toNameAndAddress(input), type);
            }
        });
    }
    
    public static class NameAndAddress {
        // "address" or "name <address>"
        private static final Pattern NAME_ADDRESS_EXTRACTOR = 
            Pattern.compile("([^<]+)(?: <(.+)>)?");

        private final String name;
        private final String address;

        private NameAndAddress(String address) {
            this(null, address);
        }
        
        private NameAndAddress(String name, String address) {
            this.name = name;
            this.address = checkNotNull(address, "address");
        }
        
        public static NameAndAddress toNameAndAddress(String address) {
            checkArgument(isValid(address), "'%s' is not a valid email address", address);
            Matcher matcher = NAME_ADDRESS_EXTRACTOR.matcher(address);
            // also sets matching groups
            checkState(matcher.matches(), "'%s' does not match required pattern '%s'",
                    address, NAME_ADDRESS_EXTRACTOR);
            String addressIfNamePresent = matcher.group(2);
            return ((addressIfNamePresent != null)
                    ? new NameAndAddress(matcher.group(1), addressIfNamePresent)
                    : new NameAndAddress(matcher.group(1)));
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }
    }
    
    public static class Recipient {
        private final NameAndAddress nameAndAddress;
        private final RecipientType type;
        
        @VisibleForTesting
        protected Recipient(NameAndAddress nameAndAddress, RecipientType type) {
            this.nameAndAddress = nameAndAddress;
            this.type = checkNotNull(type, "type");
        }

        public Email addToEmail(Email email) {
            email.addRecipient(nameAndAddress.getName(), nameAndAddress.getAddress(), type);
            return email;
        }
    }
}
