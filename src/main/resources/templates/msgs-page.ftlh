<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html>
<head>
    <title>JetStream Messages Display</title>
    <link rel="stylesheet" href="/css/index.css">
    <style>
        #logo {
            position: absolute;
            top: 3%;
            right: 2%;
            width: 10%;
        }
        .message {
            padding-top: 10px;
            display: flex;
            align-items: center;
            margin-bottom: 20px;
            font-family: 'Roboto', sans-serif;
        }

        /* Styles for the Show/Hide button within the message */
        .message__toggle-button {
            background: #5C6BC0;
            color: white;
            border: none;
            padding: 10px 15px;
            border-radius: 4px;
            cursor: pointer;
            margin-right: 10px;
            width: 100px;
            text-align: center;
        }

        .message__toggle-button:hover {
            background: #3F51B5;
        }

        .message__header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            width: 100%;
        }

        .message__details {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            font-size: 0.9em;
        }

        .message__detail {
            margin-right: 15px;
            font-size: 1.1em;
            font-weight: bold;
            color: #333;
        }

        /* Styles for the collapsible content */
        .message__content {
            background: white;
            border: 1px solid #ced4da;
            border-radius: 4px;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
            margin-bottom: 10px;
            white-space: pre-wrap;
            word-wrap: break-word;
            overflow: hidden;
            max-height: 500px;
            padding: 10px;
            transition: max-height 0.2s ease-out, padding 0.2s ease-out;
            opacity: 1;
            overflow-y: auto;
        }

        .message__content, .message__header {
            width: 800px;
            max-width: 100%;
            box-sizing: border-box;
        }

        .controls {
            padding-top: 10px;
            padding-bottom: 10px;
        }

        .controls__forms {
            display: flex;
            align-items: center;
        }

        .controls__form {
            display: flex;
            align-items: center;
        }

        .controls__field {
            display: flex;
            align-items: center;
            margin-right: 10px;
        }

        .controls__label {
            margin-right: 5px;
            white-space: nowrap;
        }

        .controls__input {
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }

        .controls__button {
            background: #5C6BC0;
            color: white;
            border: none;
            padding: 10px 15px;
            border-radius: 4px;
            cursor: pointer;
        }

        .controls__button--refresh {
            margin-left: 10px;
        }

        .controls__button:hover {
            background: #3F51B5;
        }

        .page_controls__button {
            background: #5C6BC0;
            color: white;
            border: none;
            padding: 5px 10px;
            border-radius: 4px;
            cursor: pointer;
        }

        .page_controls__button:hover {
            background: #3F51B5;
        }

        .pagination {
            margin-left: 1%;
        }
    </style>
    <script>
        const htmlDecode = (input) => {
            const doc = new DOMParser().parseFromString(input, "text/html");
            return doc.documentElement.textContent;
        }

        const toggleCollapse = (contentId, button) => {
            const element = document.getElementById(contentId);
            element.classList.toggle("collapsed");

            // Update button text based on the state
            if (element.classList.contains("collapsed")) {
                button.textContent = "Show";
            } else {
                button.textContent = "Hide";
            }
        }

        const formatTimestamp = (timestamp) => {
            const date = new Date(timestamp);
            const options = {year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'};
            return date.toLocaleDateString(undefined, options);
        }
    </script>
</head>
<body>
<img id="logo" src="<@spring.url '/images/otterjet-logo.png'/>" alt="OtterJet Logo">
<h1>Messages List</h1>
<div class="controls">
    <div class="controls__forms">
        <form method="GET" action="<@spring.url '/msgs'/>" class="controls__form">
            <div class="controls__field">
                <label for="subject" class="controls__label">Subject:</label>
                <input type="text" id="subject" name="subject" value="${subject}" class="controls__input">
            </div>
            <div class="controls__field">
                <label for="type" class="controls__label">Message type:</label>
                <input type="text" id="type" name="type" value="${type}" class="controls__input">
            </div>
            <div class="controls__field">
                <label for="type" class="controls__label">Body filter:</label>
                <input type="text" id="bodyContent" name="bodyContent" value="${bodyContent}" class="controls__input">
            </div>
            <input type="hidden" id="page" name="page" value="${page}">
            <input type="hidden" id="size" name="size" value="${size}">
            <button type="submit" class="controls__button">Filter Messages</button>
            <button type="submit" class="controls__button controls__button--refresh">Refresh</button>
        </form>
        <!-- Add pagination controls -->
        <div class="pagination">
            <button class="page_controls__button" onclick="document.getElementById('page').value = Math.max(0, ${page} - 1); document.forms[0].submit();">Previous</button>
            <span>Page ${page + 1}</span>
            <button class="page_controls__button" onclick="document.getElementById('page').value = ${page} + 1; document.forms[0].submit();">Next</button>
        </div>
    </div>
</div>

<#list messages as message>
    <div class="message">
        <div class="message__header">
            <div class="message__details">
                <div class="message__detail">Subject: ${message.subject()}</div>
                <div class="message__detail">Timestamp: <span class="message__timestamp"
                                                              data-timestamp="${message.timestamp()}"></span></div>
                <#if message.name()?has_content>
                    <div class="message__detail">Type: ${message.name()}</div>
                </#if>
            </div>
            <button class="message__toggle-button" onclick="toggleCollapse('collapsible-${message_index}', this)"
                    id="toggle-button-${message_index}">Hide
            </button>
        </div>
    </div>
    <pre id="collapsible-${message_index}" class="message__content"></pre>

    <script>
        try {
            const decodedMessage = htmlDecode('${message.body()?json_string}');
            const messageData = JSON.parse(decodedMessage);
            document.getElementById('collapsible-${message_index}').textContent = JSON.stringify(messageData, null, 4);
        } catch (error) {
            document.getElementById('collapsible-${message_index}').textContent = '${message.body()}';
        }

        document.addEventListener('DOMContentLoaded', (event) => {
            document
                .querySelectorAll('.message__timestamp')
                .forEach((elem) => {
                    const timestamp = elem.getAttribute('data-timestamp');
                    elem.textContent = formatTimestamp(timestamp);
            });
        });
    </script>
</#list>
<nav>
    <ul>
        <li><a href="<@spring.url '/'/>">Back</a></li>
    </ul>
</nav>
</body>
</html>
