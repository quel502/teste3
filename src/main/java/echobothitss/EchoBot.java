// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package echobothitss;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the useers would be added. For this
 * sample, the {@link #onMessageActivity(TurnContext)} echos the text back to the user. The {@link
 * #onMembersAdded(List, TurnContext)} will send a greeting to new conversation participants.
 * </p>
 */

@Service
public class EchoBot extends ActivityHandler {

    private String appId;
    private String appPassword;
    @Autowired
    private BotFrameworkHttpAdapter adapter ;

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        String teamsChannelId = turnContext.getActivity().teamsGetChannelId();
        Activity message = MessageFactory.text("This will start a new thread in a channel");
        String serviceUrl = turnContext.getActivity().getServiceUrl();
        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(appId, appPassword);

        ObjectNode channelData = JsonNodeFactory.instance.objectNode();
        channelData.set(
            "channel",
            JsonNodeFactory.instance.objectNode()
                .set("id", JsonNodeFactory.instance.textNode(teamsChannelId))
        );

        ConversationParameters conversationParameters = new ConversationParameters();
        conversationParameters.setIsGroup(true);
        conversationParameters.setActivity(message);
        conversationParameters.setChannelData(channelData);


        return adapter.createConversation(teamsChannelId,
            serviceUrl,
            credentials,
            conversationParameters,
            (tc) -> {
                ConversationReference reference = tc.getActivity().getConversationReference();
                return tc.getAdapter().continueConversation(
                    appId,
                    reference,
                    (continue_tc) -> continue_tc.sendActivity(
                        MessageFactory.text(
                            "This will be the first response to the new thread"
                        )
                    ).thenApply(resourceResponse -> null)
                );
            }
        ).thenApply(started -> null);
    } 

    @Override
    protected CompletableFuture<Void> onMembersAdded(
        List<ChannelAccount> membersAdded,
        TurnContext turnContext
    ) {
        return membersAdded.stream()
            .filter(
                member -> !StringUtils
                    .equals(member.getId(), turnContext.getActivity().teamsGetChannelId())
            ).map(channel -> turnContext.sendActivity(MessageFactory.text("Hello and welcome!")))
            .collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
    }
    
    public CompletableFuture<Void> SendProactiveMessage(TurnContext turnContext){
        return null;
    }

}