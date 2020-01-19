using System.CommandLine;
using System.CommandLine.Invocation;

namespace Tinkoff.VoiceKit
{
    class Program
    {
        static void Main(string[] args)
        {
            var commandRoot = new RootCommand("voicekit");
            var commandRecognize = CommandLineInterface.CreateRecognizeCommand();
            var commandStreamingRecognize = CommandLineInterface.CreateStreamingRecognizeCommand();
            var commandStreamingSynthesize = CommandLineInterface.CreateStreamingSynthesizeCommand();
            
            commandRoot.AddCommand(commandRecognize);
            commandRoot.AddCommand(commandStreamingRecognize);
            commandRoot.AddCommand(commandStreamingSynthesize);
            
            commandRoot.InvokeAsync(args).Wait();
        }
    }
}
